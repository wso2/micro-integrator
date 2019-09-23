package artifactUtils

type TemplateList struct {
	SequenceTemplates []Template `json:"sequenceTemplateList"`
	EndpointTemplates []Template `json:"endpointTemplateList"`
}

type TemplateListByType struct {
	Count     int32      `json:"count"`
	Templates []Template `json:"list"`
}

type TemplateListByName struct {
	Parameters []string `json:"Parameters"`
	Name       string   `json:"name"`
}

type Template struct {
	Name string `json:"name"`
}
