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



import { Checkbox, RaisedButton, Snackbar, TextField } from 'material-ui';
import { MuiThemeProvider } from 'material-ui/styles';
import React, { Component } from 'react';
import { FormattedMessage } from 'react-intl';
import FormPanel from '../common/FormPanel';
import Header from '../common/Header';

import defaultTheme from '../utils/Theme';

/**
 * Style constants.
 */
const styles = {
    cookiePolicy: {
        padding: '10px',
        fontFamily: defaultTheme.fontFamily,
        border: '1px solid #8a6d3b',
        color: '#8a6d3b'
    },
    cookiePolicyAnchor: {
        fontWeight: 'bold',
        color: '#8a6d3b'
    },
    contentDiv: {
        backgroundColor:'black',
        height:'100vh'
    },
};


/**
 * Login page.
 */
export default class Login extends Component {
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
    }

    /**
     * Render default login page.
     *
     * @return {XML} HTML content
     */
    renderDefaultLogin() {
        const { username, password, host, port } = this.state;
        return (
            <MuiThemeProvider muiTheme={defaultTheme}>
                <div style={styles.contentDiv}>
                    <Header
                        title={<FormattedMessage id='portal.title' defaultMessage='Micro Integrator' />}
                        rightElement={<span />}
                    />
                    <FormPanel
                        title={<FormattedMessage id="login.title" defaultMessage="Login" />}
                    >
                        <TextField
                            autoFocus
                            fullWidth
                            autoComplete="off"
                            floatingLabelText={<FormattedMessage id="login.host" defaultMessage="Host"/>}
                            value={host}
                            onChange={(e) => {
                                this.setState({
                                    host: e.target.value,
                                });
                            }}
                        />
                        <br />
                        <TextField
                            autoFocus
                            fullWidth
                            autoComplete="off"
                            floatingLabelText={<FormattedMessage id="login.port" defaultMessage="Port"/>}
                            value={port}
                            onChange={(e) => {
                                this.setState({
                                    port: e.target.value,
                                });
                            }}
                        />
                        <br />
                        <TextField
                            autoFocus
                            fullWidth
                            autoComplete="off"
                            floatingLabelText={<FormattedMessage id="login.username" defaultMessage="Username"/>}
                            value={username}
                            onChange={(e) => {
                                this.setState({
                                    username: e.target.value,
                                });
                            }}
                        />
                        <br />
                        <TextField
                            fullWidth
                            type="password"
                            autoComplete="off"
                            floatingLabelText={<FormattedMessage id="login.password" defaultMessage="Password"/>}
                            value={password}
                            onChange={(e) => {
                                this.setState({
                                    password: e.target.value,
                                });
                            }}
                        />
                        <br />
                        <Checkbox
                            label={<FormattedMessage id="login.rememberMe" defaultMessage="Remember Me"/>}
                            checked={this.state.rememberMe}
                            onCheck={(e, checked) => {
                                this.setState({
                                    rememberMe: checked,
                                });
                            }}
                            style={{ margin: '30px 0' }}
                        />
                        <br />
                        <RaisedButton
                            primary
                            type="submit"
                            disabled={username === '' || password === ''|| host === '' || port ===''}
                            label={<FormattedMessage id="login.title" defaultMessage="Login"/>}
                            disabledBackgroundColor="rgb(27, 40, 47)"
                        />
                        <br />
                        <br />
                        <div>
                            <div style={styles.cookiePolicy}>
                                <FormattedMessage
                                    id="login.cookie.policy.before"
                                    defaultMessage="After a successful sign in, we use a cookie in your browser to
                                    track your session. You can refer our "
                                />
                                <a style={styles.cookiePolicyAnchor} href=""
                                    target="_blank"
                                >
                                    <FormattedMessage id="login.cookie.policy" defaultMessage="Cookie Policy"/>
                                </a >
                                <FormattedMessage id="login.cookie.policy.after" defaultMessage=" for more details."/>
                            </div>
                        </div>
                        <br />
                        <div style= {styles.cookiePolicy}>
                            <div>
                                <FormattedMessage
                                    id="login.privacy.policy.before"
                                    defaultMessage="By signing in, you agree to our "
                                />
                                <a style={styles.cookiePolicyAnchor}
                                    href=""
                                    target="_blank">
                                    <FormattedMessage id="login.privacy.policy" defaultMessage="Privacy Policy"/>
                                </a>
                                <FormattedMessage id="login.privacy.policy.after" defaultMessage="."/>
                            </div>
                        </div>
                    </FormPanel>
                    <Snackbar
                        message={this.state.error}
                        open={this.state.showError}
                        autoHideDuration="4000"
                        onRequestClose={() => this.setState({ error: '', showError: false })}
                    />
                </div>
            </MuiThemeProvider>
        );
    }



    /**
     * Renders the login page.
     *
     * @return {XML} HTML content
     */
    render() {
        return this.renderDefaultLogin();
    }
}


