install:
	npm install
	cp node_modules/ipfs-css/ipfs.css src/ui/less/ipfs.css
	cp node_modules/tachyons/css/tachyons.css src/ui/less/tachyons.css
	cp -r node_modules/ipfs-css/fonts resources/public/fonts
