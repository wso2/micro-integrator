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
import { BrowserRouter} from 'react-router-dom';
import { Route, Switch } from 'react-router';

import Login from './auth/Login';
import Logout from './auth/Logout';
import SecuredRouter from './auth/SecuredRouter';

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
                    <Route component={SecuredRouter} />
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
