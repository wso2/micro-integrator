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
import Qs from 'qs';

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
import SequenceDetailsPage from "../resource-pages/SequenceDetailsPage";
import MessageProcessorDetailPage from "../resource-pages/MessageProcessorDetailPage";
import APIDetailsPage from "../resource-pages/ApiDetailsPage";
import ProxyDetailsPage from "../resource-pages/ProxyDetailsPage";
import HomePage from "../resource-pages/HomePage";
import MessageStoreDetailsPage from "../resource-pages/MessageStoreDetailsPage";
import EndpointDetailsPage from "../resource-pages/EndpointDetailsPage";
import TaskDetailsPage from "../resource-pages/TaskDetailsPage";
import LocalEntryDetailsPage from "../resource-pages/LocalEntryDetailsPage";
import InboundEndpointDetailPage from "../resource-pages/InboundEndpointDetailsPage";
import TaskListPage from "../resource-pages/TaskListPage";

export default class SecuredRouter extends Component {


    constructor() {
        super();
        this.handleSessionInvalid = this.handleSessionInvalid.bind(this);
        window.handleSessionInvalid = this.handleSessionInvalid;
    }

    handleSessionInvalid() {
        this.forceUpdate();
    }

    render() {

        // if the user is not logged in Redirect to login
        if (!AuthManager.isLoggedIn()) {
            let referrer = this.props.location.pathname;
            const arr = referrer.split('');
            if (arr[arr.length - 1] !== '/') {
                referrer += '/';
            }
            const params = Qs.stringify({referrer});
            return (
                <Redirect to={{pathname: '/login', search: params}}/>
            );
        }

        return (
            <Switch>
                <Route exact path='/proxy' component={ProxyServiceListPage}/>
                <Route exact path='/endpoint' component={EndpointListPage}/>
                <Route path='/endpoint/explore' component={EndpointDetailsPage}/>
                <Route exact path='/message-processor' component={MessageProcessorListPage}/>
                <Route path='/message-processor/explore' component={MessageProcessorDetailPage}/>
                <Route exact path='/message-store' component={MessageStoreListPage}/>
                <Route path='/message-store/explore' component={MessageStoreDetailsPage}/>
                <Route exact path='/api' component={APIListPage}/>
                <Route path='/api/explore' component={APIDetailsPage}/>
                <Route path='/proxy/sourceView' component={ProxySourceViewPage}/>
                <Route path='/proxy/explore' component={ProxyDetailsPage}/>
                <Route exact path='/connector' component={ConnectorListPage}/>
                <Route exact path='/template' component={TemplateListPage}/>
                <Route exact path='/inbound-endpoint' component={InboundEndpointListPage}/>
                <Route path='/inbound-endpoint/explore' component={InboundEndpointDetailPage}/>
                <Route exact path='/local-entry' component={LocalEntryListPage}/>
                <Route path='/local-entry/explore' component={LocalEntryDetailsPage}/>
                <Route exact path='/sequence' component={SequenceListPage}/>
                <Route path='/sequence/explore' component={SequenceDetailsPage}/>
                <Route path='/api/sourceView' component={ApiSourceViewPage}/>
                <Route exact path='/task' component={TaskListPage}/>
                <Route path='/task/explore' component={TaskDetailsPage}/>
                <Route exact path='/home' component={HomePage}/>
            </Switch>
        );
    }
}