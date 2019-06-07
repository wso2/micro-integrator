//+build ignore

package main

import (
	"github.com/wso2/micro-integrator/cmd/cmd"
	"log"
	"os"
	"path/filepath"
)

func main() {

	err := os.MkdirAll("shell-completions", os.ModePerm)
	if err != nil {
		log.Fatal(err)
	}

	log.Println("Generating bash completions...")
	err = cmd.RootCmd.GenBashCompletionFile(filepath.FromSlash("./shell-completions/mi_bash_completion.sh"))
	if err != nil {
		log.Fatal(err)
	}
}
