#!/bin/sh

tag="$1"
if [ -z "${tag}" ]; then
    echo "no tag given."
    echo "usage: $0 tag"
    exit 1
fi

name="PhotoNote-${tag}"

git archive --format=tar --prefix=${name}/ ${tag} | xz > ${name}.tar.xz

# vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
