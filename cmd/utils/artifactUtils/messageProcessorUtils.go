package artifactUtils

type MessageProcessorList struct {
	Count             int32              `json:"count"`
	MessageProcessors []MessageProcessor `json:"list"`
}

type MessageProcessor struct {
	Name   string `json:"name"`
	Type   string `json:"type"`
	Status string `json:"status"`
}

type MessageProcessorData struct {
	Name       string            `json:"name"`
	FileName   string            `json:"fileName"`
	Type       string            `json:"type"`
	Store      string            `json:"messageStore"`
	Container  string            `json:"artifactContainer"`
	Parameters map[string]string `json:"parameters"`
	Status     string            `json:"status"`
}

func (messageProcessors *MessageProcessorList) GetDataIterator() <-chan []string {
	ch := make(chan []string)

	go func() {
		for _, messageProcessor := range messageProcessors.MessageProcessors {
			ch <- []string{messageProcessor.Name, messageProcessor.Type, messageProcessor.Status}
		}
		close(ch)
	}()

	return ch
}

func (messageProcessors *MessageProcessorList) GetCount() int32 {
	return messageProcessors.Count
}
