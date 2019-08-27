/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import React, {Component} from 'react';
import AuthManager from './utils/AuthManager';
import {Route, Redirect, Switch} from 'react-router';

import MessageStoreListPage from "../resource-pages/MessageStoreListPage";
import ProxySourceViewPage from "../resource-pages/ProxySourceViewPage";
import APIListPage from "../resource-pages/ApiListPage";
import SequenceListPage from "../resource-pages/SequenceListPage";
import LocalEntryListPage from "../resource-pages/LocalEntryListPage";
import InboundEndpointListPage from "../resource-pages/InboundEndpointListPage";
import EndpointListPage from "../resource-pages/EndpointListPage";
import MessageProcessorListPage from "../resource-pages/MessageProcessorListPage";
import ProxyServiceListPage from "../resource-pages/ProxyServiceListPage";
import ApiSourceViewPage from "../resource-pages/APISourceViewPage";
import ConnectorListPage from "../resource-pages/ConnectorListPage";
import TemplateListPage from "../resource-pages/TemplateListPage";

export default class SecuredRouter extends Component {

    render() {
        // if the user is not logged in Redirect to login
        if (!AuthManager.isLoggedIn()) {

            return (
                <Redirect to='/login'/>
            );
        }

        return (
            <Switch>
                <Route exact path='/proxy' component={ProxyServiceListPage}/>
                <Route exact path='/endpoint' component={EndpointListPage}/>
                <Route exact path='/message-processor' component={MessageProcessorListPage}/>
                <Route exact path='/message-store' component={MessageStoreListPage}/>
                <Route exact path='/api' component={APIListPage}/>
                <Route path='/proxy/sourceView' component={ProxySourceViewPage}/>
                <Route exact path='/connector' component={ConnectorListPage}/>
                <Route exact path='/template' component={TemplateListPage}/>
                <Route exact path='/inbound-endpoint' component={InboundEndpointListPage}/>
                <Route exact path='/local-entry' component={LocalEntryListPage}/>
                <Route exact path='/sequence' component={SequenceListPage}/>
                <Route path='/api/sourceView' component={ApiSourceViewPage}/>
            </Switch>
        );
    }
}