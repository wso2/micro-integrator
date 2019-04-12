#!/bin/bash
#
# usage: ./build.bash target-file.go

# argument handling
test "$1" && target="$1" # .go file to build

if ! test "$target"
then
  echo "Target file required"
  exit 1
fi

binary="" # default to default
test "$2" && binary="$2" # binary output


# find available build types
platforms=`ls $(go env GOROOT)/pkg | grep -v "obj\|tool\|race"`

if ! test "$platforms"; then
  echo "no valid os/arch pairs were found to build"
  echo "- see: https://gist.github.com/jmervine/7d3f455e923cf2ac3c9e#file-golang-crosscompile-setup-bash"
  exit 1
fi

for platform in ${platforms}
do
    split=(${platform//_/ })
    goos=${split[0]}
    goarch=${split[1]}

    # ensure output file name
    output="$binary"
    test "$output" || output="$(basename $target | sed 's/\.go//')"

    # add exe to windows output
    [[ "windows" == "$goos" ]] && output="$output.exe"

    # set destination path for binary
    destination="$(dirname $target)/builds/$goos/$goarch/$output"

    echo "GOOS=$goos GOARCH=$goarch go build -x -o $destination $target"
    GOOS=$goos GOARCH=$goarch go build -x -o $destination $target
done

echo "Build complete!"