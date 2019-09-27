package artifactUtils

type CompositeAppList struct {
	Count         int32                 `json:"count"`
	CompositeApps []CompositeAppSummary `json:"list"`
}

type CompositeAppSummary struct {
	Name    string `json:"name"`
	Version string `json:"version"`
}

type CompositeApp struct {
	Name      string     `json:"name"`
	Version   string     `json:"version"`
	Artifacts []Artifact `json:"artifacts"`
}

type Artifact struct {
	Name string `json:"name"`
	Type string `json:"type"`
}

func (compositeApps *CompositeAppList) GetDataIterator() <-chan []string {
	ch := make(chan []string)

	go func() {
		for _, compositeApp := range compositeApps.CompositeApps {
			ch <- []string{compositeApp.Name, compositeApp.Version}
		}
		close(ch)
	}()

	return ch
}

func (compositeApps *CompositeAppList) GetCount() int32 {
	return compositeApps.Count
}
