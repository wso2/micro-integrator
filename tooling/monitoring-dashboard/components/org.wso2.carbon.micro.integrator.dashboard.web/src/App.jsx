/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import React, { Component } from 'react';
import ReactDOM from 'react-dom';
// import { IntlProvider } from 'react-intl';
import { BrowserRouter} from 'react-router-dom';
import { Route, Switch } from 'react-router';

import Login from './auth/Login';
import Logout from './auth/Logout';

import APIListPage from './resource-pages/ApiListPage';
import ApiSourceViewPage from './resource-pages/APISourceViewPage';
import ConnectorListPage from './resource-pages/ConnectorListPage';
import EndpointListPage from './resource-pages/EndpointListPage';
import InboundEndpointListPage from './resource-pages/InboundEndpointListPage';
import LocalEntryListPage from './resource-pages/LocalEntryListPage';
import MessageProcessorListPage from './resource-pages/MessageProcessorListPage';
import MessageStoreListPage from './resource-pages/MessageStoreListPage';
import ProxyServiceListPage from './resource-pages/ProxyServiceListPage';
import ProxyDetailsPage from './resource-pages/ProxyDetailsPage';
import ProxySourceViewPage from './resource-pages/ProxySourceViewPage';
import SequenceListPage from './resource-pages/SequenceListPage';
import TemplateListPage from './resource-pages/TemplateListPage';
import TaskListPage from './resource-pages/TaskListPage';
import MessageStoreDetailsPage from './resource-pages/MessageStoreDetailsPage';
import MessageProcessorDetailPage from './resource-pages/MessageProcessorDetailPage';
import APIDetailsPage from './resource-pages/ApiDetailsPage';
import LocalEntryDetailsPage from './resource-pages/LocalEntryDetailsPage';
import InboundEndpointDetailPage from './resource-pages/InboundEndpointDetailsPage';
import EndpointDetailsPage from './resource-pages/EndpointDetailsPage';
import SequenceDetailsPage from './resource-pages/SequenceDetailsPage';
import TaskDetailsPage from './resource-pages/TaskDetailsPage';
import HomePage from './resource-pages/HomePage';

import '../public/css/dashboard.css';


class App extends Component {
    constructor() {
        super();
    }

    render() {
        return (
            <BrowserRouter basename={window.contextPath}>
                <Switch>
                    {/* Authentication */}
                    <Route exact path='/login' component={Login} />
                    <Route exact path='/logout' component={Logout} />
                    {/* Secured routes */}


                    <Route exact path='/proxy' component={ProxyServiceListPage} />
                    <Route exact path='/endpoint' component={EndpointListPage} />
                    <Route path='/endpoint/explore' component={EndpointDetailsPage} />
                    <Route exact path='/message-processor' component={MessageProcessorListPage} />
                    <Route path='/message-processor/explore' component={MessageProcessorDetailPage} />
                    <Route exact path='/message-store' component={MessageStoreListPage} />
                    <Route path='/message-store/explore' component={MessageStoreDetailsPage} />
                    <Route exact path='/api' component={APIListPage} />
                    <Route path='/api/explore' component={APIDetailsPage} />
                    <Route path='/proxy/sourceView' component={ProxySourceViewPage} />
                    <Route path='/proxy/explore' component={ProxyDetailsPage} />
                    <Route exact path='/connector' component={ConnectorListPage} />
                    <Route exact path='/template' component={TemplateListPage} />
                    <Route exact path='/inbound-endpoint' component={InboundEndpointListPage} />
                    <Route path='/inbound-endpoint/explore' component={InboundEndpointDetailPage} />
                    <Route exact path='/local-entry' component={LocalEntryListPage} />
                    <Route path='/local-entry/explore' component={LocalEntryDetailsPage} />
                    <Route exact path='/sequence' component={SequenceListPage} />
                    <Route path='/sequence/explore' component={SequenceDetailsPage} />
                    <Route path='/api/sourceView' component={ApiSourceViewPage} />
                    <Route exact path='/task' component={TaskListPage} />
                    <Route path='/task/explore' component={TaskDetailsPage} />

                    <Route exact path='/home' component={HomePage} />
                    {/* Secured routes */}
                    {/*<Route component={SecuredRouter} />*/}
                    <Route component={AnonPage}/>
                </Switch>
            </BrowserRouter>
        );
    }
};

class AnonPage extends Component {
    render() {
        return (
            <h1>This is anon page.</h1>
        );
    }
};

ReactDOM.render(<App/>, document.getElementById('content'));
