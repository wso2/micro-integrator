package artifactUtils

type EndpointList struct {
	Count     int32             `json:"count"`
	Endpoints []EndpointSummary `json:"list"`
}

type EndpointSummary struct {
	Name   string `json:"name"`
	Type   string `json:"type"`
	Method string `json:"method"`
	Url    string `json:"url"`
}

type Endpoint struct {
	Name   string `json:"name"`
	Type   string `json:"type"`
	Method string `json:"method"`
	Url    string `json:"url"`
	Stats  string `json:"stats"`
}

func (endpoints *EndpointList) GetDataIterator() <-chan []string {
	ch := make(chan []string)

	go func() {
		for _, endpoint := range endpoints.Endpoints {
			ch <- []string{endpoint.Name, endpoint.Type, endpoint.Method, endpoint.Url}
		}
		close(ch)
	}()

	return ch
}

func (endpoints *EndpointList) GetCount() int32 {
	return endpoints.Count
}
