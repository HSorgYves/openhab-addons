/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import static org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass.*;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessagePriority;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the Association Group Info Command command class.
 *
 * @author Jorg de Jong
 */
@XStreamAlias("associationGroupInfoCommandClass")
public class ZwaveAssociationGroupInfoCommandClass extends ZWaveCommandClass
        implements ZWaveCommandClassInitialization {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZwaveAssociationGroupInfoCommandClass.class);

    private static final byte GROUP_NAME_GET = 0x01;
    private static final byte GROUP_NAME_REPORT = 0x02;
    private static final byte INFO_GET = 0x03;
    private static final byte INFO_REPORT = 0x04;
    private static final byte COMMAND_LIST_GET = 0x05;
    private static final byte COMMAND_LIST_REPORT = 0x06;

    private static final int MAX_STRING_LENGTH = 25;

    private static final int GET_LISTMODE_MASK = 0x40;
    // not used private static final int GET_REFRESHCACHE_MASK = 0x80;

    private static final int REPORT_GROUPCOUNT_MASK = 0x3f;
    private static final int REPORT_DYNAMICINFO_MASK = 0x40;
    private static final int REPORT_LISTMODE_MASK = 0x80;

    private static final byte PROFILE_GENERAL = 0x00;
    // general sub profile
    private static final byte PROFILE_LIFELINE = 0x01;

    // list of groups that the controller should subscribe to.
    private Set<Integer> autoSubscribeGroups = new HashSet<>();;
    private Map<Integer, GroupInfo> groups = new HashMap<>();

    // list of command classes that are eligible for auto subscription.
    @XStreamOmitField
    private Set<CommandClass> autoCCs = ImmutableSet.of(DEVICE_RESET_LOCALLY, BATTERY, CONFIGURATION, METER,
            THERMOSTAT_OPERATING_STATE, THERMOSTAT_MODE, THERMOSTAT_FAN_MODE, SENSOR_MULTILEVEL, SENSOR_ALARM,
            THERMOSTAT_FAN_STATE, THERMOSTAT_SETPOINT, SENSOR_BINARY, ALARM, COLOR, SCENE_ACTIVATION, CENTRAL_SCENE,
            DOOR_LOCK, METER_TBL_MONITOR, METER_PULSE);

    /**
     * Creates a new instance of the ZwaveAssociationGroupInfoCommandClass class.
     *
     * @param node
     *            the node this command class belongs to
     * @param controller
     *            the controller to use
     * @param endpoint
     *            the endpoint this Command class belongs to
     */
    public ZwaveAssociationGroupInfoCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.ASSOCIATION_GROUP_INFO;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpointId)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received ASSOCIATION_GROUP_INFO command V{}", getNode().getNodeId(), getVersion());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case GROUP_NAME_REPORT:
                logger.trace("NODE {}: Process Group Name Report", getNode().getNodeId());
                processGroupNameReport(serialMessage, offset);
                break;
            case INFO_REPORT:
                logger.trace("NODE {}: Process Group Info Report", getNode().getNodeId());
                processInfoReport(serialMessage, offset);
                break;
            case COMMAND_LIST_REPORT:
                logger.trace("NODE {}: Process Group Command List Report", getNode().getNodeId());
                processCommandListReport(serialMessage, offset);
                break;
            default:
                logger.warn("NODE {}: Unsupported Command {} for command class {} ({}).", getNode().getNodeId(),
                        command, getCommandClass().getLabel(), getCommandClass().getKey());

        }
    }

    private void processGroupNameReport(SerialMessage serialMessage, int offset) throws ZWaveSerialMessageException {
        int groupIdx = serialMessage.getMessagePayloadByte(offset + 1);
        int numBytes = serialMessage.getMessagePayloadByte(offset + 2);

        if (numBytes < 0) {
            logger.error("NODE {}: Group Name report error in message length ({})", getNode().getNodeId(),
                    serialMessage.getMessagePayload().length);
        }

        if (numBytes == 0) {
            getGroupInfo(groupIdx).setName("");
            return;
        }

        if (numBytes > MAX_STRING_LENGTH) {
            logger.warn("NODE {}: Group Name is too big; maximum is {} characters {}", getNode().getNodeId(),
                    MAX_STRING_LENGTH, numBytes);
            numBytes = MAX_STRING_LENGTH;
        }

        // Check for non-printable characters - ignore anything after the first one!
        for (int c = 0; c < numBytes; c++) {
            if (serialMessage.getMessagePayloadByte(c + offset + 3) == 0) {
                numBytes = c;
                logger.debug("NODE {}: Group name string truncated to {} characters", getNode().getNodeId(), numBytes);
                break;
            }
        }
        byte[] strBuffer = Arrays.copyOfRange(serialMessage.getMessagePayload(), offset + 3, offset + 3 + numBytes);
        String groupName = null;
        try {
            groupName = new String(strBuffer, "ASCII");
        } catch (UnsupportedEncodingException e) {
            logger.debug("exeption during group name extraction ", e);
        }

        logger.debug("NODE {}: recieved group name: '{}' for group number: {}", getNode().getNodeId(), groupName,
                groupIdx);

        getGroupInfo(groupIdx).setName(groupName);
    }

    private void processInfoReport(SerialMessage serialMessage, int offset) throws ZWaveSerialMessageException {
        boolean listMode = (serialMessage.getMessagePayloadByte(offset + 1) & REPORT_LISTMODE_MASK) != 0;
        boolean dynamicInfo = (serialMessage.getMessagePayloadByte(offset + 1) & REPORT_DYNAMICINFO_MASK) != 0;

        // number of group info elements in this message. Not the total number of groups on the device.
        // The device can send multiple reports in list mode
        int groupCount = serialMessage.getMessagePayloadByte(offset + 1) & REPORT_GROUPCOUNT_MASK;
        logger.debug("NODE {}: AssociationGroupInfoCmd_Info_Report: count:{} listMode:{} dynamicInfo:{}",
                getNode().getNodeId(), groupCount, listMode, dynamicInfo);
        for (int i = 0; i < groupCount; i++) {
            int groupIdx = serialMessage.getMessagePayloadByte(offset + 2 + i * 7);
            int mode = serialMessage.getMessagePayloadByte(offset + 3 + i * 7);
            int profile_msb = serialMessage.getMessagePayloadByte(offset + 4 + i * 7);
            int profile_lsb = serialMessage.getMessagePayloadByte(offset + 5 + i * 7);
            int profile = (profile_msb << 8 | profile_lsb);

            logger.debug("NODE {}:    Group={}, Profile={}, mode:{}", getNode().getNodeId(), groupIdx, profile, mode);

            getGroupInfo(groupIdx).setProfile(profile);

            if ((profile_msb == PROFILE_GENERAL) && (profile_lsb == PROFILE_LIFELINE)) {
                autoSubscribeGroups.add(groupIdx);
            }
        }
    }

    private void processCommandListReport(SerialMessage serialMessage, int offset) throws ZWaveSerialMessageException {
        // list the CommandClasses and commands that will be send to the associated nodes in this group.
        // for now just log it, later this could be used auto associate to the group
        // ie always associate when we find a battery command class
        int groupid = serialMessage.getMessagePayloadByte(offset + 1);
        int size = serialMessage.getMessagePayloadByte(offset + 2);
        logger.debug("NODE {}: Supported Command classes and commands for group:{} ->", getNode().getNodeId(), groupid);
        Set<CommandClass> commands = new HashSet<>();
        for (int i = 0; i < size; i += 2) {

            // check if this node actually supports this Command Class
            ZWaveCommandClass cc = getNode()
                    .getCommandClass(CommandClass.getCommandClass(serialMessage.getMessagePayloadByte(offset + 3 + i)));

            if (cc != null) {
                logger.debug("NODE {}:   {} command:{}", getNode().getNodeId(), cc.getCommandClass(),
                        serialMessage.getMessagePayloadByte(offset + i + 4));
                commands.add(cc.getCommandClass());
                if (autoCCs.contains(cc.getCommandClass())) {
                    autoSubscribeGroups.add(groupid);
                }
            } else {
                logger.debug("NODE {}:   unsupported {} command:{}", getNode().getNodeId(),
                        serialMessage.getMessagePayloadByte(offset + 3 + i),
                        serialMessage.getMessagePayloadByte(offset + 4 + i));
            }
        }
        getGroupInfo(groupid).setCommands(commands);
    }

    /**
     * Gets a SerialMessage with the GROUP_NAME_GET command
     *
     * @return the serial message
     */
    public SerialMessage getGroupNameMessage(int groupidx) {
        logger.debug("NODE {}: Creating new message for application command GROUP_NAME_GET for group {}",
                getNode().getNodeId(), groupidx);
        SerialMessage result = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(getNode().getNodeId());
        outputData.write(3);
        outputData.write(getCommandClass().getKey());
        outputData.write(GROUP_NAME_GET);
        outputData.write(groupidx);
        result.setMessagePayload(outputData.toByteArray());

        return result;
    }

    /**
     * Gets a SerialMessage with the INFO_GET command
     *
     * @return the serial message
     */
    public SerialMessage getInfoMessage(int groupidx) {
        logger.debug("NODE {}: Creating new message for application command INFO_GET for group {}",
                getNode().getNodeId(), groupidx);
        SerialMessage result = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);

        byte listMode = 0;
        if (groupidx == 0) {
            // request all groups
            listMode = GET_LISTMODE_MASK;
        }

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(getNode().getNodeId());
        outputData.write(4);
        outputData.write(getCommandClass().getKey());
        outputData.write(INFO_GET);
        outputData.write(listMode);
        outputData.write(groupidx);

        result.setMessagePayload(outputData.toByteArray());

        return result;
    }

    /**
     * Gets a SerialMessage with the COMMAND_LIST_GET command
     *
     * @return the serial message
     */
    public SerialMessage getCommandListMessage(int groupidx) {
        logger.debug("NODE {}: Creating new message for application command COMMAND_LIST_GET for group {}",
                getNode().getNodeId(), groupidx);
        SerialMessage result = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);

        byte allowCache = 0;

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(getNode().getNodeId());
        outputData.write(4);
        outputData.write(getCommandClass().getKey());
        outputData.write(COMMAND_LIST_GET);
        outputData.write(allowCache);
        outputData.write(groupidx);

        result.setMessagePayload(outputData.toByteArray());

        return result;
    }

    /**
     * Get the groups the controller should be subscribed to.
     *
     * @return a set of group numbers
     */
    public Set<Integer> getAutoSubscribeGroups() {
        return autoSubscribeGroups;
    }

    /**
     * Get the collected Association Group Info data.
     *
     * @return a immutable copy of the data
     */
    public Map<Integer, GroupInfo> getGroupInfo() {
        return ImmutableMap.copyOf(groups);
    }

    @Override
    public Collection<SerialMessage> initialize(boolean refresh) {
        ArrayList<SerialMessage> result = new ArrayList<SerialMessage>();

        // we need the number of groups as discovered by the AssociationCommandClass
        ZWaveAssociationCommandClass associationClass = (ZWaveAssociationCommandClass) getNode().resolveCommandClass(
                ZWaveCommandClass.CommandClass.ASSOCIATION, getEndpoint() == null ? 0 : getEndpoint().getEndpointId());

        if (associationClass != null) {
            logger.debug("NODE {}: max group: {} endpoint: {}", getNode().getNodeId(), associationClass.getMaxGroups(),
                    getEndpoint() == null ? 0 : getEndpoint().getEndpointId());
            // for each group request its name and other info
            // only request it if we have not received an answer yet
            for (int i = 1; i <= associationClass.getMaxGroups(); i++) {
                GroupInfo info = getGroupInfo(i);
                if (refresh == true || info.getName() == null) {
                    result.add(getGroupNameMessage(i));
                }
                if (refresh == true || info.getProfile() == null) {
                    result.add(getInfoMessage(i));
                }
                if (refresh == true || info.getCommands() == null) {
                    result.add(getCommandListMessage(i));
                }
            }
        }

        return result;
    }

    private GroupInfo getGroupInfo(int groupidx) {
        if (!groups.containsKey(groupidx)) {
            groups.put(groupidx, new GroupInfo());
        }
        return groups.get(groupidx);
    }

    /**
     * Class to hold group info
     *
     */
    @XStreamAlias("associationGroupInfo")
    public class GroupInfo {
        private String name;
        private Integer profile;
        private Set<CommandClass> commands;

        public Set<CommandClass> getCommands() {
            return commands;
        }

        public void setCommands(Set<CommandClass> commands) {
            this.commands = commands;
        }

        public Integer getProfile() {
            return profile;
        }

        public void setProfile(Integer profile) {
            this.profile = profile;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
