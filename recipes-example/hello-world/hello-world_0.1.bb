DESCRIPTION = "Ricardicus hello world program" 
HOMEPAGE = "https://ricardicus.se"
LICENSE = "CLOSED"

SRCREV = "343aa3614f636e10607f52ed59e39316f7560ca2"
SRC_URI = "git://git@github.com/Ricardicus/hello-world.git"

S = "${WORKDIR}/git"

inherit meson 

DEPENDS = "python3 meson-native"

MESON_BUILDTYPE_${PN} = "release"

USERADD_PACKAGES = "${PN}"

FILES_${PN} = "/usr/bin/helloworld"

IMAGE_INSTALL_append = " hello-world"


