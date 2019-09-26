package artifactUtils

type InboundEndpointList struct {
	Count            int32                    `json:"count"`
	InboundEndpoints []InboundEndpointSummary `json:"list"`
}

type InboundEndpointSummary struct {
	Name string `json:"name"`
	Type string `json:"protocol"`
}

type InboundEndpoint struct {
	Name       string      `json:"name"`
	Type       string      `json:"protocol"`
	Stats      string      `json:"stats"`
	Tracing    string      `json:"tracing"`
	Parameters []Parameter `json:"parameters"`
}

type Parameter struct {
	Name  string `json:"name"`
	Value string `json:"value"`
}

func (inboundEndpoints *InboundEndpointList) GetDataIterator() <-chan []string {
	ch := make(chan []string)

	go func() {
		for _, inboundEndpoint := range inboundEndpoints.InboundEndpoints {
			ch <- []string{inboundEndpoint.Name, inboundEndpoint.Type}
		}
		close(ch)
	}()

	return ch
}

func (inboundEndpoints *InboundEndpointList) GetCount() int32 {
	return inboundEndpoints.Count
}
