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

import {
    MDBContainer,
    MDBRow,
    MDBCol,
    MDBCard,
    MDBCardBody,
    MDBModalFooter,
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
            //Refresh token
        }
    }

    authenticate(e) {
        const { intl } = this.context;
        const {username, password, host, port, rememberMe} = this.state;
        e.preventDefault();
        AuthManager.authenticate(host, port, username, password, rememberMe)
            .then(() => this.setState({authenticated: true}))
            .catch((error) => {
                alert(error);
                console.log(error.response);
                const errorMessage = error.response && error.response.status === 401
                    ? 'Invalid username/password!'
                    : 'Unknown error occurred!'
                alert(errorMessage);
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
            <MDBContainer>
                <MDBRow>
                    <MDBCol md="6" style={styles.LoginForm}>
                        <MDBCard>
                            <MDBCardBody>
                                <MDBCardHeader className="form-header warm-flame-gradient rounded">
                                    <h3 className="my-3">
                                        <MDBIcon icon="lock" /> Micro Integrator Login:
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
                                />

                                <div className="text-center mt-4">
                                    <MDBBtn color="deep-orange" className="mb-3" type="submit" >
                                        Login
                                    </MDBBtn>
                                </div>
                            </MDBCardBody>
                        </MDBCard>
                    </MDBCol>
                </MDBRow>
            </MDBContainer>
        );
    }


    /**
     * Renders the login page.
     *
     * @return {XML} HTML content
     */
    render() {
        // const authenticated = this.state.authenticated;
        // if (authenticated) {
        //     return (
        //         <Redirect to='/home'/>
        //     );
        // }
        return this.renderDefaultLogin();
    }
}
