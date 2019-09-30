package artifactUtils

import "strconv"

type MessageStoreList struct {
	Count         int32          `json:"count"`
	MessageStores []MessageStore `json:"list"`
}

type MessageStore struct {
	Name string `json:"name"`
	Size int    `json:"size"`
	Type string `json:"type"`
}

type MessageStoreData struct {
	Name       string            `json:"name"`
	FileName   string            `json:"file"`
	Container  string            `json:"container"`
	Properties map[string]string `json:"properties"`
	Producer   string            `json:"producer"`
	Consumer   string            `json:"consumer"`
	Size       int               `json:"size"`
}

func (messageStores *MessageStoreList) GetDataIterator() <-chan []string {
	ch := make(chan []string)

	go func() {
		for _, messageStore := range messageStores.MessageStores {
			ch <- []string{messageStore.Name, messageStore.Type, strconv.Itoa(messageStore.Size)}
		}
		close(ch)
	}()

	return ch
}

func (messageStores *MessageStoreList) GetCount() int32 {
	return messageStores.Count
}
