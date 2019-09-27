package artifactUtils

type LocalEntryList struct {
	Count        int32        `json:"count"`
	LocalEntries []LocalEntry `json:"list"`
}

type LocalEntry struct {
	Name string `json:"name"`
	Type string `json:"type"`
}

type LocalEntryData struct {
	Name  string `json:"name"`
	Type  string `json:"type"`
	Value string `json:"value"`
}

func (localEntries *LocalEntryList) GetDataIterator() <-chan []string {
	ch := make(chan []string)

	go func() {
		for _, localEntry := range localEntries.LocalEntries {
			ch <- []string{localEntry.Name, localEntry.Type}
		}
		close(ch)
	}()

	return ch
}

func (localEntries *LocalEntryList) GetCount() int32 {
	return localEntries.Count
}
