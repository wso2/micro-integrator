package artifactUtils

type Proxy struct {
	Name    string `json:"name"`
	Wsdl11  string `json:"wsdl1_1"`
	Wsdl20  string `json:"wsdl2_0"`
	Stats   string `json:"stats"`
	Tracing string `json:"tracing"`
}

type ProxyServiceList struct {
	Count   int32          `json:"count"`
	Proxies []ProxySummary `json:"list"`
}

type ProxySummary struct {
	Name   string `json:"name"`
	Wsdl11 string `json:"wsdl1_1"`
	Wsdl20 string `json:"wsdl2_0"`
}

func (data *ProxyServiceList) GetDataIterator() <-chan []string {
	ch := make(chan []string)

	go func() {
		for _, proxy := range data.Proxies {
			ch <- []string{proxy.Name, proxy.Wsdl11, proxy.Wsdl20}
		}
		close(ch)
	}()

	return ch
}

func (data *ProxyServiceList) GetCount() int32 {
	return data.Count
}
