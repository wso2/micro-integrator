import React from 'react';
import {makeStyles} from '@material-ui/core/styles';
import Drawer from '@material-ui/core/Drawer';
import CssBaseline from '@material-ui/core/CssBaseline';
import List from '@material-ui/core/List';
import Divider from '@material-ui/core/Divider';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import {Link} from "react-router-dom";
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
import TaskIcon from '@material-ui/icons/Today';

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
        height: 'calc(100% - 50px)'
    },
    toolbar: theme.mixins.toolbar,
}));

export default function SideDrawer() {
    const classes = useStyles();

    return (
        <div className={classes.root}>
            <CssBaseline/>
            <Drawer
                className={classes.drawer}
                variant="permanent"
                classes={{
                    paper: classes.drawerPaper,
                }}
                anchor="left"
            >
                <div className={classes.toolbar}/>
                <List>
                    <ListItem button component={Link} to="/proxy">
                        <ListItemIcon><NotesIcon color="error"/></ListItemIcon>
                        <ListItemText primary="Proxy services"/>
                    </ListItem>
                    <ListItem button component={Link} to="/endpoint">
                        <ListItemIcon><SendIcon color="error"/></ListItemIcon>
                        <ListItemText primary="Endpoints"/>
                    </ListItem>
                    <ListItem button component={Link} to="/inbound-endpoint">
                        <ListItemIcon><TransformIcon color="error"/></ListItemIcon>
                        <ListItemText primary="Inbound Endpoints"/>
                    </ListItem>
                    <ListItem button component={Link} to="/message-processor">
                        <ListItemIcon><MessageProcessorIcon color="error"/></ListItemIcon>
                        <ListItemText primary="Message Processors"/>
                    </ListItem>
                    <ListItem button component={Link} to="/message-store">
                        <ListItemIcon><MessageStoreIcon color="error"/></ListItemIcon>
                        <ListItemText primary="Message Stores"/>
                    </ListItem>
                    <ListItem button component={Link} to="/api">
                        <ListItemIcon><ApiIcon color="error"/></ListItemIcon>
                        <ListItemText primary="API"/>
                    </ListItem>
                    <ListItem button component={Link} to="/template">
                        <ListItemIcon><TemplateIcon color="error"/></ListItemIcon>
                        <ListItemText primary="Templates"/>
                    </ListItem>
                    <ListItem button component={Link} to="/sequence">
                        <ListItemIcon><SequenceIcon color="error"/></ListItemIcon>
                        <ListItemText primary="Sequences"/>
                    </ListItem>
                    <ListItem button component={Link} to="/task">
                        <ListItemIcon><TaskIcon color="error"/></ListItemIcon>
                        <ListItemText primary="Tasks"/>
                    </ListItem>
                    <ListItem button component={Link} to="/local-entry">
                        <ListItemIcon><LocalEntryIcon color="error"/></ListItemIcon>
                        <ListItemText primary="Local Entries"/>
                    </ListItem>
                    <ListItem button component={Link} to="/connector">
                        <ListItemIcon><ConnectorIcon color="error"/></ListItemIcon>
                        <ListItemText primary="Connectors"/>
                    </ListItem>
                </List>
                <Divider/>

            </Drawer>
        </div>
    );
}
