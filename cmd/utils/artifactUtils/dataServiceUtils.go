package artifactUtils

type DataServicesList struct {
	Count int32                `json:"count"`
	List  []DataServiceSummary `json:"list"`
}

type DataServiceInfo struct {
	ServiceName        string         `json:"serviceName"`
	ServiceDescription string         `json:"serviceDescription"`
	ServiceGroupName   string         `json:"serviceGroupName"`
	Wsdl11             string         `json:"wsdl1_1"`
	Wsdl20             string         `json:"wsdl2_0"`
	Queries            []QuerySummary `json:"queries"`
}

type DataServiceSummary struct {
	ServiceName string `json:"name"`
	Wsdl11      string `json:"wsdl1_1"`
	Wsdl20      string `json:"wsdl2_0"`
}

type QuerySummary struct {
	Id        string `json:"id"`
	Namespace string `json:"namespace"`
}

func (data *DataServicesList) GetDataIterator() <-chan []string {
	ch := make(chan []string)

	go func() {
		for _, val := range data.List {
			ch <- []string{val.ServiceName, val.Wsdl11, val.Wsdl20}
		}
		close(ch)
	}()

	return ch
}

func (data *DataServicesList) GetCount() int32 {
	return data.Count
}
