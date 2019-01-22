🚧🚧🚧🚧 WARNING: This is a pre-alpha experiment. Expect drastic changes as we iterate and learn. 🚧🚧🚧🚧

<h1 align="center" title="cube">
  Cube
</h1>

<p align="center">
  <a href="https://protocol.io"><img src="https://img.shields.io/badge/made%20by-Protocol%20Labs-blue.svg?style=flat-square" /></a>
  <a href="http://peerpad.net/"><img src="https://img.shields.io/badge/project-Cube-blue.svg?style=flat-square" /></a>
  <a href="http://webchat.freenode.net/?channels=%23ipfs"><img src="https://img.shields.io/badge/freenode-%23ipfs-blue.svg?style=flat-square" /></a>
</p>

> IPFS Cube will help people deploy and manage their own IPFS pinning services
> on top of existing cheap hardware, or cloud storage.

Note: This is meant to be a tool that anyone can use. Protocol Labs/IPFS is
*not* running its own pinning service.

## What is the problem we’re trying to solve?

* Users need to run their own IPFS pinning services easily.
* Running an IPFS node on its own, or even running an IPFS Cluster, doesn’t
  accomplish much for everyday users. For a large portion of current IPFS users,
  and for a much larger set of potential adopters, you need to be able to run
  and manage your own IPFS pinning service -- allocating storage, deciding who
  can pin stuff, seeing what’s been pinned (and by whom), observing how that
  service contributes to the overall IPFS network, and managing the pinned
  content over time.

This combination of uses is not only compelling on its own, it’s also a basis
for a plethora of more specific IPFS products and tools that could address use
cases like users of apps like [PeerPad](https://peerpad.net) who need a place to
pin their documents and edits government agencies publishing open data and
measuring impact of the data research labs pinning and redistributing data
they’ve produced or relied on libraries pinning, cataloging, indexing, and
preserving the data their patrons rely on participants in a community wifi
network maintaining content for their local peers to access independently of
broader internet connectivity

All of these use cases depend on reliable, easy to use, easily managed pinning
services.

* We need to know our users.
* IPFS, along with the broader peer-to-peer web, will only gain broad adoption
  when these technologies feel robust, reliable, and predictable while also
  offering clear, novel benefits that centralized infrastructure can’t provide.
  While technical features play some role in achieving this quality of experience,
  adoption will ultimately rely on delivering consistent, high quality User
  Experience. This requires sustained, focused, iterative user discovery, design,
  and development. Currently we don’t have enough fine-grained information about
  our users, their needs, nor how they think about the data they share on the
  web. This prevents us from carrying out the design and iterative delivery of
  a product that will achieve those goals.

We need a thread that weaves together all the dependencies and interacting
modules across Protocol Labs to create a cohesive path towards broader user
adoption of our technologies. We need to make it easier for people to make the
choice between local pinning (just running `ipfs pin` locally), remote centralized
pinning (pinning with Cube), and remote replicated pinning (pinning with Cluster).

## The vision

*What would it mean to run a Cube?*

Running an IPFS Cube is running an IPFS pinning service that has
members/subscribers, controls for managing usage, and a great UX overall. By
default a Cube will be backed by an IPFS Cluster, but it can be re-configured
to use other storage services such as Microsoft Azure, the Cloudflare IPFS
gateway, or Amazon S3.

An example of a target user for IPFS Cube might be a tech-savvy librarian at a
public library, who already runs a couple websites and is an admin on their
Drupal instance. They probably have sysadmin privileges on a server or two, but
their real motivation is to support their community’s ability to store, share,
access and preserve knowledge. They want to run a pinning service for their
community and would prefer to have a complete experience from install through
management and most debugging that doesn’t involve hacking around on the command
line.

This is just one example of a target user. Our first effort will focus on
choosing their optimal set of target users and identifying real people who we
can engage with as exemplars of the use cases.

*How does this relate to IPFS Cluster?*

Cube will be a tool that many people will use together with IPFS Cluster. They
will use Cube to run and manage a pinning service and, by default, that Cube
will create an IPFS Cluster to handle storage. Because of this relationship,
Cube’s use cases strongly overlap with some of the use cases for Cluster. If we
are successful at building a great UX for Cube, thousands or millions of people
will use Cube to run and manage their own IPFS Clusters, with Cube as their
main interface while Cluster functions primarily as an underlying system that
they don’t have to worry about unless they choose to.

Running a Cluster is running a group of nodes with some strategy for
understanding what content gets pinned. The target users are system
administrators of some sort who are comfortable with ideas like virtualization,
kubernetes, and CLIs. By contrast, running a Cube gives you a pinning service
with tools for managing and monitoring the service, its users, its configuration,
etc. By default a Cube will spin up an IPFS Cluster as its storage layer. Its
target users are mainly focused on the people pinning data on their Cube, the
costs and benefits of running the Cube, and quality of service rather than
focusing on the storage equipment, pinning strategies, etc. They prefer non-CLI
tools and might not have deep familiarity with ideas like virtualization,
kubernetes, etc.

Building out Cube is good for Cluster product development because we will have
the opportunity to grapple with what should (or should not) be in the core
Cluster product. For example, for a basic Cube to work, it needs to have things
like:

seamless automated creation and configuration of an IPFS Cluster through simple UI
authentication and authenticated pinning
per user and per group of user allocation of storage
reporting on storage usage
tooling for configuration (how are you going to store this stuff, how redundant)

To implement these features well, we need end user input for both Cluster and
Cube. Building Cube will help disambiguate which functionality lives in which
module for it, and other third-party services seeking to use Cluster to support
their work.

## Team

* [Michelle Hertzfeld](https://github.com/meiqimichelle)
* [Victor Bjelkholm](https://github.com/victorb)

## Contribute

Cube is a work in progress. As such, there's a few things you can do right now to help out:

* **[Check out the existing issues](https://github.com/ipfs-shipyard/cube/issues)**!
* **Perform code reviews**. More eyes will help a) speed the project along b) ensure quality and c) reduce possible future bugs.
* **Add tests**. There can never be enough tests.

Read Cube [contributing.md](docs/CONTRIBUTING.md) for details on the latest development flow.

### Want to hack on Cube?

[![](https://cdn.rawgit.com/jbenet/contribute-ipfs-gif/master/img/contribute.gif)](https://github.com/ipfs/community/blob/master/contributing.md)

# License

The MIT License (MIT)

Copyright (c) 2017 Protocol Labs Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
