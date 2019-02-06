variable "do_token" {}

provider "digitalocean" {
  token = "${var.do_token}"
}

resource "digitalocean_droplet" "web" {
  image  = "ubuntu-18-04-x64"
  name   = "cube"
  region = "sfo2"

  # size     = "s-1vcpu-1gb"
  size     = "s-6vcpu-16gb"
  ssh_keys = [14693619, 20585995]
}

resource "null_resource" "app" {
  triggers = {
    web_ids = "${join(",", digitalocean_droplet.web.*.id)}"
  }

  connection {
    host = "${element(digitalocean_droplet.web.*.ipv4_address, count.index)}"
  }

  provisioner "remote-exec" {
    inline = [
      "fallocate -l 1G /swapfile || true",
      "chmod 600 /swapfile || true",
      "mkswap /swapfile || true",
      "swapon /swapfile || true",
      "mkdir /app || true",
    ]
  }

  provisioner "local-exec" {
    command     = "lein uberjar"
    working_dir = "../"
  }

  provisioner "local-exec" {
    command     = "cp target/cube-0.2.0-SNAPSHOT-standalone.jar infra/cube.jar"
    working_dir = "../"
  }

  provisioner "file" {
    source      = "cube.jar"
    destination = "/app/cube.jar"
  }

  provisioner "remote-exec" {
    inline = [
      "curl https://get.docker.com | bash",
    ]
  }

  # provisioner "file" {
  #   source      = ".env"
  #   destination = "/app/.env"
  # }

  provisioner "file" {
    source      = "services/cube.service"
    destination = "/etc/systemd/system/cube.service"
  }
  provisioner "remote-exec" {
    inline = [
      "apt update && apt install --yes default-jre",
      "systemctl daemon-reload",
      "systemctl restart cube",
      "echo cube is now running",
    ]
  }
}

output "ip" {
  value = "${digitalocean_droplet.web.ipv4_address}"
}
