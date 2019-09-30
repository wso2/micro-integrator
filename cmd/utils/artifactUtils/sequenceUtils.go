package artifactUtils

type SequenceList struct {
	Count     int32             `json:"count"`
	Sequences []SequenceSummary `json:"list"`
}

type SequenceSummary struct {
	Name      string `json:"name"`
	Container string `json:"container"`
	Stats     string `json:"stats"`
	Tracing   string `json:"tracing"`
}

type Sequence struct {
	Name      string   `json:"name"`
	Container string   `json:"container"`
	Stats     string   `json:"stats"`
	Tracing   string   `json:"tracing"`
	Mediators []string `json:"mediators"`
}

func (sequences *SequenceList) GetDataIterator() <-chan []string {
	ch := make(chan []string)

	go func() {
		for _, sequence := range sequences.Sequences {
			ch <- []string{sequence.Name, sequence.Stats, sequence.Tracing}
		}
		close(ch)
	}()

	return ch
}

func (sequences *SequenceList) GetCount() int32 {
	return sequences.Count
}
