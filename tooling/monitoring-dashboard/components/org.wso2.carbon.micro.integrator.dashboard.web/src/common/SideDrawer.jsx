import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Drawer from '@material-ui/core/Drawer';
import CssBaseline from '@material-ui/core/CssBaseline';
import List from '@material-ui/core/List';
import Divider from '@material-ui/core/Divider';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import { Link } from "react-router-dom";
import ListItemIcon from '@material-ui/core/ListItemIcon';

import NotesIcon from '@material-ui/icons/Notes';
import SendIcon from '@material-ui/icons/Send';
import TransformIcon from '@material-ui/icons/Transform';
import MessageStoreIcon from '@material-ui/icons/StoreMallDirectory';
import MessageProcessorIcon from '@material-ui/icons/Message';
import ConnectorIcon from '@material-ui/icons/CastConnected';
import ApiIcon from '@material-ui/icons/Apps';
import LocalEntryIcon from '@material-ui/icons/Assignment';
import SequenceIcon from '@material-ui/icons/CompareArrows';
import TemplateIcon from '@material-ui/icons/Description';

const drawerWidth = 240;

const useStyles = makeStyles(theme => ({
    root: {
        display: 'flex',
    },
    drawer: {
        width: drawerWidth,
        flexShrink: 0,
    },
    drawerPaper: {
        width: drawerWidth,
    },
    toolbar: theme.mixins.toolbar,
}));

export default function SideDrawer() {
    const classes = useStyles();

    return (
        <div className={classes.root}>
            <CssBaseline />
            <Drawer
                className={classes.drawer}
                variant="permanent"
                classes={{
                    paper: classes.drawerPaper,
                }}
                anchor="left"
            >
                <div className={classes.toolbar} />
                <List>
                    <ListItem button component={Link} to="/proxy">
                        <ListItemIcon><NotesIcon/></ListItemIcon>
                        <ListItemText primary="Proxy services"/>
                    </ListItem>
                    <ListItem button component={Link} to="">
                        <ListItemIcon><SendIcon/></ListItemIcon>
                        <ListItemText primary="Endpoints"/>
                    </ListItem>
                    <ListItem button component={Link} to="">
                        <ListItemIcon><TransformIcon/></ListItemIcon>
                        <ListItemText primary="Inbound Endpoints"/>
                    </ListItem>
                    <ListItem button component={Link} to="">
                        <ListItemIcon><MessageProcessorIcon/></ListItemIcon>
                        <ListItemText primary="Message Processors"/>
                    </ListItem>
                    <ListItem button component={Link} to="">
                        <ListItemIcon><MessageStoreIcon/></ListItemIcon>
                        <ListItemText primary="Message Stores"/>
                    </ListItem>
                    <ListItem button component={Link} to="">
                        <ListItemIcon><ApiIcon/></ListItemIcon>
                        <ListItemText primary="API"/>
                    </ListItem>
                    <ListItem button component={Link} to="">
                        <ListItemIcon><TemplateIcon/></ListItemIcon>
                        <ListItemText primary="Templates"/>
                    </ListItem>
                    <ListItem button component={Link} to="">
                        <ListItemIcon><SequenceIcon/></ListItemIcon>
                        <ListItemText primary="Sequences"/>
                    </ListItem>
                    <ListItem button component={Link} to="">
                        <ListItemIcon><LocalEntryIcon/></ListItemIcon>
                        <ListItemText primary="Local Entries"/>
                    </ListItem>
                    <ListItem button component={Link} to="">
                        <ListItemIcon><ConnectorIcon/></ListItemIcon>
                        <ListItemText primary="Connectors"/>
                    </ListItem>
                </List>
                <Divider/>

            </Drawer>
        </div>
    );
}
