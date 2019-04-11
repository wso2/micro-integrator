#!/bin/bash

showUsageAndExit () {
    echo "Insufficient or invalid options provided"
    echo
    echo "Usage: "$'\e[1m'"./build.sh -t [target-file] -v [build-version] -f"$'\e[0m'
    echo -en "  -t\t"
    echo "[REQUIRED] Target file to build."
    echo -en "  -v\t"
    echo "[REQUIRED] Build version. If not specified a default value will be used."
    echo -en "  -f\t"
    echo "[OPTIONAL] Cross compile for all the list of platforms. If not specified, the specified target file will be cross compiled only for the autodetected native platform."
    echo
    echo "Ex: "$'\e[1m'"./build.sh -t micli.go -v 1.0.0 -f"$'\e[0m'" - Builds MI Management CLI for version
     1.0.0 for all the platforms"
    echo
    exit 1
}

detectPlatformSpecificBuild () {
    platform=$(uname -s)
    if [[ "${platform}" == "Linux" ]]; then
        platforms="linux/386/linux/i586 linux/amd64/linux/x64"
    elif [[ "${platform}" == "Darwin" ]]; then
        platforms="darwin/amd64/macosx/x64"
    else
        platforms="windows/386/windows/i586 windows/amd64/windows/x64"
    fi
}


while getopts :t:v:f FLAG; do
  case $FLAG in
    t)
      target=$OPTARG
      ;;
    v)
      build_version=$OPTARG
      ;;
    f)
      full_build="true"
      ;;
    \?)
      showUsageAndExit
      ;;
  esac
done

if [ ! -e "$target" ]; then
  echo "Target file is needed. "
  showUsageAndExit
  exit 1
fi

if [ -z "$build_version" ]
then
  echo "Build version is needed. "
  showUsageAndExit
fi


rootPath=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
buildDir="build/target"
buildPath="$rootPath/${buildDir}"

echo "Cleaning build path ${buildDir}..."
rm -rf $buildPath

filename=$(basename ${target})
baseDir=$(dirname ${target})

if [ ".go" == ${filename:(-3)} ]
then
    filename=${filename%.go}
fi

#platforms="darwin/amd64 freebsd/386 freebsd/amd64 freebsd/arm linux/386 linux/amd64 linux/arm windows/386 windows/amd64"
#platforms="linux/amd64/linux/x64"
#platforms="darwin/amd64/macosx/x64"
if [ "${full_build}" == "true" ]; then
# following line causes an error in MacOS
#    echo "Building "$'\e[1m'"${filename^^}:${build_version}"$'\e[0m'" for all platforms..."
    platforms="darwin/amd64/macosx/x64 linux/386/linux/i586 linux/amd64/linux/x64 windows/386/windows/i586 windows/amd64/windows/x64"
else
    detectPlatformSpecificBuild
    echo "Building "$'\e[1m'"${filename^^}:${build_version}"$'\e[0m'" for detected "$'\e[1m'"${platform}"$'\e[0m'" platform..."
fi

for platform in ${platforms}
do
    split=(${platform//\// })
    goos=${split[0]}
    goarch=${split[1]}
    pos=${split[2]}
    parch=${split[3]}

    # ensure output file name
    output="micli"
    test "$output" || output="$(basename ${target} | sed 's/\.go//')"

    # add exe to windows output
    [[ "windows" == "$goos" ]] && output="$output.exe"

    echo -en "\t - $goos/$goarch..."

    zipfile="$filename-$build_version-$pos-$parch"
    zipdir="${buildPath}/$filename"
    mkdir -p $zipdir

    # Do we need this ?
    # cp -r "${baseDir}/resources/README.html" $zipdir > /dev/null 2>&1
    cp -r "${baseDir}/LICENSE" $zipdir > /dev/null 2>&1

    # set destination path for binary
    destination="$zipdir/$output"

    echo ${destination}

    #echo "GOOS=$goos GOARCH=$goarch go build -x -o $destination $target"
    # GOOS=$goos GOARCH=$goarch go build -gcflags=-trimpath=$GOPATH -asmflags=-trimpath=$GOPATH -ldflags "-X mi-cli.Version=$build_version -X 'mi-cli.buildDate=$(date -u '+%Y-%m-%d
    # %H:%M:%S UTC')'" -o $destination $target
    
    # ==================== CHECK THIS ==============================================
    # cp -r "${baseDir}/resources/main_config.yaml" "${zipdir}" > /dev/null 2>&1

    pwd=`pwd`
    cd $buildPath
    if [[ "windows" == "$goos" ]]; then
    	zip -r "$zipfile.zip" $filename > /dev/null 2>&1
    else
    	tar czf "$zipfile.tar.gz" $filename > /dev/null 2>&1
    fi
    rm -rf $filename
    cd $pwd
    echo -en $'\e[1m\u2714\e[0m'
    echo
done

echo "Build complete!"