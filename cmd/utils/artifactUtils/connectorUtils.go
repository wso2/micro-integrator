package artifactUtils

type ConnectorList struct {
	Count      int32              `json:"count"`
	Connectors []ConnectorSummary `json:"list"`
}

type ConnectorSummary struct {
	Name        string `json:"name"`
	Status      string `json:"status"`
	Package     string `json:"package"`
	Description string `json:"description"`
}

func (connectors *ConnectorList) GetDataIterator() <-chan []string {
	ch := make(chan []string)

	go func() {
		for _, connector := range connectors.Connectors {
			ch <- []string{connector.Name, connector.Status, connector.Package, connector.Description}
		}
		close(ch)
	}()

	return ch
}

func (connectors *ConnectorList) GetCount() int32 {
	return connectors.Count
}
