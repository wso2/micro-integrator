package artifactUtils

type TaskList struct {
	Count int32  `json:"count"`
	Tasks []Task `json:"list"`
}

type Task struct {
	Name            string `json:"name"`
	Type            string `json:"triggerType"`
	TriggerCount    string `json:"triggerCount"`
	TriggerInterval string `json:"triggerInterval"`
	TriggerCron     string `json:"triggerCron"`
}

func (tasks *TaskList) GetDataIterator() <-chan []string {
	ch := make(chan []string)

	go func() {
		for _, task := range tasks.Tasks {
			ch <- []string{task.Name, task.Type, task.TriggerCount, task.TriggerInterval, task.TriggerCron}
		}
		close(ch)
	}()

	return ch
}

func (tasks *TaskList) GetCount() int32 {
	return tasks.Count
}
