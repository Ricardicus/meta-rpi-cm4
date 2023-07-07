Yocto layer for cross compilation to RPI CM4 (armv7)
====================================================

## Finding the right Yocto release

The Linux distribution for our raspberry Pi CM4* is Rasbian.
To build for Rasbian, we need the glibc version on target.
After having ssh:ed into the device we run ldd:

```bash
$ ldd --version
ldd (Debian GLIBC 2.28-10+rpt2+rpi1+deb10u2) 2.28
Copyright (C) 2018 Free Software Foundation, Inc.
This is free software; see the source for copying conditions.  There is NO
warranty; not even for MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
Written by Roland McGrath and Ulrich Drepper.
```

We find that it is 2.28. A matching Yocto release for this is
"thud" from Nov 2018. It has glibc version 2.28 in it
(poky/meta/recipes-core/glibc/glibc_2.28.bb).

We therefore use poky "thud" release.

* http://datasheets.raspberrypi.com/cm4/cm4-datasheet.pdf?_gl=1*lo7nef*_ga*MTAwNzEwNDkxMS4xNjgxMzg0MTI5*_ga_22FD70LWDS*MTY4Nzc3MTc0MC4xLjEuMTY4Nzc3MTc2Mi4wLjAuMA..

## Example program

We will build a simple example program for the given hardware (armv7) and software platform (Rasbian).
The program will be built with the meson build system which Yocto Thud supports.

## Setting up the build environment

In order to set up the build environment, we use Yocto "thud" release.
We need to apply a patch to the binutils-cross-arm package in order to compile without errors.

```bash
# Clone the thud release of Yocto
git clone http://git.yoctoproject.org/git/poky -b thud
# go in
cd poky
# clone this repo into the workspace
git clone git@github.com:Ricardicus/meta-rpi-example.git
```

Launch the oe-init-build-env, copy the configuration
from this repo (meta-rpi-example) to the build folder. Edit the bblayers path so
that it suits the environment (using sed in this example).

```bash
# source the build script (this will also cd into build)
source oe-init-build-env
# Copy conf files
cp ../meta-rpi-example/base-build/*.conf conf
# Set correct paths
sed -i "s#BUILDDIR#$BUILDDIR#g" conf/bblayers.conf
```

Before we go any further, we need to apply a patch.

There is a bug in binutils-cross-arm that needs to be fixed (by adding a missing #include <string>).
We need to fetch the sources of binutils and then apply this patch.

Fetch all the sources for the binutils program.

```bash
# Fetch the binutils sources, this can take a minute (be patient)
bitbake -c fetch binutils-cross-arm
# Unpack the fetched source files
bitbake -c unpack binutils-cross-arm
# Apply the patch (by overwriting the source file..)
cp ../meta-rpi-example/gold/errors.h tmp/work/x86_64-linux/binutils-cross-arm/2.31.1-r0/git/gold/errors.h
```

Now build the hello-world program (a simple program). This can take a while.

```bash
bitbake hello-world
```

## Transporting the file to target

We can transport the binary application file to target using scp. Our raspberry pi
has the IP address 192.168.100.100 on our network. We copy the file like this:

```bash
# After entering the password, the file is sent to the home directory
scp ./tmp/work/cortexa7t2hf-neon-vfpv4-poky-linux-gnueabi/hello-world/0.1-r0/image/usr/bin/helloworld user@192.168.100.100:/home/user/helloworld
```

We use ssh to enter the device shell. In here, we can run the program:

```bash
./helloworld
# outputs:
# Hello, World!
```

# Building a toolchain

Having done the steps above, the target meta-toolchain is available to create a toolchain installer.

```bash
bitbake meta-toolchain
```

The resulting toolchain script is found here:

```bash
$ ls tmp/deploy/sdk
poky-glibc-x86_64-meta-toolchain-cortexa7t2hf-neon-vfpv4-toolchain-2.6.4.host.manifest
poky-glibc-x86_64-meta-toolchain-cortexa7t2hf-neon-vfpv4-toolchain-2.6.4.sh
poky-glibc-x86_64-meta-toolchain-cortexa7t2hf-neon-vfpv4-toolchain-2.6.4.target.manifest
poky-glibc-x86_64-meta-toolchain-cortexa7t2hf-neon-vfpv4-toolchain-2.6.4.testdata.json
```
