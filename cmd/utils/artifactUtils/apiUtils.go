package artifactUtils

type API struct {
	Name      string     `json:"name"`
	Url       string     `json:"url"`
	Version   string     `json:"version"`
	Stats     string     `json:"stats"`
	Tracing   string     `json:"tracing"`
	Resources []Resource `json:"resources"`
}

type Resource struct {
	Methods []string `json:"methods"`
	Url     string   `json:"url"`
}

type APIList struct {
	Count int32        `json:"count"`
	Apis  []APISummary `json:"list"`
}

type APISummary struct {
	Name string `json:"name"`
	Url  string `json:"url"`
}

func (apis *APIList) GetDataIterator() <-chan []string {
	ch := make(chan []string)

	go func() {
		for _, api := range apis.Apis {
			ch <- []string{api.Name, api.Url}
		}
		close(ch)
	}()

	return ch
}

func (apis *APIList) GetCount() int32 {
	return apis.Count
}
