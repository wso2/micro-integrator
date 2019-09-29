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
import {Redirect} from 'react-router';
import "@fortawesome/fontawesome-free/css/all.min.css";
import "bootstrap-css-only/css/bootstrap.min.css";
import "mdbreact/dist/css/mdb.css";
import {Snackbar} from 'material-ui';
import {MuiThemeProvider} from 'material-ui/styles';
import Header from '../common/Header';

import {
    MDBContainer,
    MDBRow,
    MDBCol,
    MDBCard,
    MDBCardBody,
    MDBIcon,
    MDBCardHeader,
    MDBBtn
} from "mdbreact";
import defaultTheme from "../utils/Theme";

/**
 * Login page.
 */

const styles = {
    LoginForm: {
        margin: '0 auto',
        paddingTop: '240px'
    },
    formHeader: {
        backgroundColor:'#ffffff'
    }
};
export default class Login2 extends Component {
    /**
     * Constructor.
     *
     * @param {{}} props Props
     */
    constructor(props) {
        super(props);
        this.state = {
            username: '',
            password: '',
            host: '',
            port: '',
            authenticated: false,
            rememberMe: false,
        };
        this.authenticate = this.authenticate.bind(this);
    }


    componentWillMount() {
        this.initAuthenticationFlow();
    }

    /**
     * Check if the user has already signed in and remember me is set
     */
    initAuthenticationFlow() {
        if (!AuthManager.isLoggedIn()) {
            this.setState({authenticated: false})
        } else {
            this.setState({authenticated: true})
        }
    }

    authenticate(e) {
        const { intl } = this.context;
        const {username, password, host, port, rememberMe} = this.state;
        e.preventDefault();
        AuthManager.authenticate(host, port, username, password, rememberMe)
            .then(() => this.setState({authenticated: true}))
            .catch((error) => {
                const errorMessage = error.response && error.response.status === 401
                    ? 'Invalid username/password!'
                    : 'Unknown error occurred!'
                this.setState({
                    username: '',
                    password: '',
                    error: errorMessage,
                    showError: true,
                });
            });

    }

    /**
     * Render default login page.
     *
     * @return {XML} HTML content
     */
    renderDefaultLogin() {
        const {username, password, host, port} = this.state;
        return (
            <MuiThemeProvider muiTheme={defaultTheme}>
                <Header
                    title={'MICRO INTEGRATOR'}
                    rightElement={<span/>}
                />
            <MDBContainer>
                <MDBRow>
                    <MDBCol md="6" style={styles.LoginForm}>
                        <MDBCard>
                            <MDBCardBody>
                                <MDBCardHeader className="form-header rgba-blue-grey-light rounded">
                                    <h3 className="my-3">
                                        <MDBIcon icon="lock" /> LOGIN
                                    </h3>
                                </MDBCardHeader>
                                <label
                                    htmlFor="defaultFormEmailEx"
                                    className="grey-text font-weight-light"
                                >
                                    Host
                                </label>
                                <input
                                    type="email"
                                    id="defaultFormEmailEx"
                                    className="form-control"
                                    value={host}
                                    onChange={(e) => {
                                        this.setState({
                                            host: e.target.value,
                                        });
                                    }}
                                />

                                <label
                                    htmlFor="defaultFormEmailEx"
                                    className="grey-text font-weight-light"
                                >
                                    Port
                                </label>
                                <input
                                    type="email"
                                    id="defaultFormEmailEx"
                                    className="form-control"
                                    value={port}
                                    onChange={(e) => {
                                        this.setState({
                                            port: e.target.value,
                                        });
                                    }}
                                />

                                <label
                                    htmlFor="defaultFormEmailEx"
                                    className="grey-text font-weight-light"
                                >
                                    User Name
                                </label>
                                <input
                                    type="email"
                                    id="defaultFormEmailEx"
                                    className="form-control"
                                    value={username}
                                    onChange={(e) => {
                                        this.setState({
                                            username: e.target.value,
                                        });
                                    }}
                                />

                                <label
                                    htmlFor="defaultFormPasswordEx"
                                    className="grey-text font-weight-light"
                                >
                                    Password
                                </label>
                                <input
                                    type="password"
                                    id="defaultFormPasswordEx"
                                    className="form-control"
                                    value={password}
                                    onChange={(e) => {
                                        this.setState({
                                            password: e.target.value,
                                        });
                                    }}
                                />

                                <div className="text-center mt-4">
                                    <MDBBtn color="blue-grey"
                                            className="mb-3"
                                            type="submit"
                                            disabled={username === '' || password === '' || host === '' || port === ''}
                                            onClick={this.authenticate}
                                    >
                                        Login
                                    </MDBBtn>
                                </div>
                            </MDBCardBody>
                        </MDBCard>
                    </MDBCol>
                </MDBRow>
                <Snackbar
                    message={this.state.error}
                    open={this.state.showError}
                    autoHideDuration="4000"
                    onRequestClose={() => this.setState({error: '', showError: false})}
                />
            </MDBContainer>
            </MuiThemeProvider>
        );
    }


    /**
     * Renders the login page.
     *
     * @return {XML} HTML content
     */
    render() {
         const authenticated = this.state.authenticated;
         if (authenticated) {
                location.href = '/dashboard/home';
        }
        return this.renderDefaultLogin();
    }
}
