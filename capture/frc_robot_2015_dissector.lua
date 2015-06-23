frc_robot_2015 = Proto("frc_robot_2015","FRC Robot Protocol")

local MODE_VALS = {[0x0] = "Teleoperated", [0x8] = "Autonomous", [0x1] = "Test"}
local DS_LCD_COMMAND_VALS = {[0x9FFF] = "Full Display Text"}
local fields = {
	flags_f = ProtoField.uint8("frc_robot_2015.flags", "Status Flags", base.HEX),
	flags = {
		reset = ProtoField.bool("frc_robot_2015.flags.reset", "Reset", 8, nil, 0x80),
		not_estopped = ProtoField.bool("frc_robot_2015.flags.not_estopped", "Not Emergency Stopped", 8, nil, 0x40),
		enabled = ProtoField.bool("frc_robot_2015.flags.enabled", "Enabled", 8, nil, 0x20),
		resync = ProtoField.bool("frc_robot_2015.flags.resync", "Resync", 8, nil, 0x4),
		mode = ProtoField.uint8("frc_robot_2015.flags.mode", "Mode", base.HEX, MODE_VALS, 0x12),
		check_versions_field = ProtoField.bool("frc_robot_2015.flags.check_versions", "Check Versions", 8, nil, 0x1)
	},
	battery_voltage = ProtoField.uint16("frc_robot_2015.battery_voltage", "Battery Voltage", base.HEX),
	digital_output_f = ProtoField.uint8("frc_robot_2015.digital_output", "Digital Outputs", base.HEX),
	digital_output = {},
	team_num = ProtoField.uint16("frc_robot_2015.team_num", "Team Number", base.DEC),
	mac_address = ProtoField.ether("frc_robot_2015.mac_address", "MAC Address"),
	version = ProtoField.string("frc_robot_2015.version", "Version"),
	pkt_num = ProtoField.uint16("frc_robot_2015.pkt_num", "Packet Number", base.DEC),
	crc = ProtoField.uint32("frc_robot_2015.crc", "CRC Checksum", base.HEX),
	ds_lcd_f = ProtoField.bytes("frc_robot_2015.ds_lcd", "Driver Station LCD"),
	ds_lcd = {
		command = ProtoField.uint16("frc_robot_2015.ds_lcd", "Command", base.HEX, DS_LCD_COMMAND_VALS),
		lines = {}
	}
}

do
	local mask = 0x1
	for i = 1, 8 do
		table.insert(fields.digital_output, ProtoField.bool("frc_robot_2015.digital_output."..i, "D0 "..i, 8, nil, mask))
		mask = mask * 2
	end
end

for i = 1, 6 do
	table.insert(fields.ds_lcd.lines, ProtoField.string("frc_robot_2015.ds_lcd.lines."..i, "Line "..i))
end

local function flatten(list)
	if type(list) ~= "table" then return {list} end
	local flat_list = {}
	for _, elem in pairs(list) do
		for _, val in pairs(flatten(elem)) do
			flat_list[#flat_list + 1] = val
		end
	end
	return flat_list
end

frc_robot_2015.fields = flatten(fields)

-- frc_robot_2015 dissector function
function frc_robot_2015.dissector (buf, pkt, root)
	-- make sure the packet is the right length
	if buf:len() ~= 1152 then return end
	pkt.cols.protocol = frc_robot_2015.name

	-- create subtree
	subtree = root:add(frc_robot_2015, buf(0))

	local flags = buf(0, 1)
	flags_subtree = subtree:add(fields.flags_f, flags)
	for i, flag in pairs(fields.flags) do
		flags_subtree:add(flag, flags)
	end
	subtree:add(fields.battery_voltage, buf(1, 2)):append_text(" ("..buf(1, 1).."."..buf(2, 1)..")")
	local digital_output = buf(3, 1)
	local digital_output_subtree = subtree:add(fields.digital_output_f, digital_output)
	for i, d in ipairs(fields.digital_output) do
		digital_output_subtree:add(d, digital_output)
	end
	subtree:add(fields.team_num, buf(8, 2))
	subtree:add(fields.mac_address, buf(10, 6))
	subtree:add(fields.version, buf(16, 8))
  	subtree:add(fields.pkt_num, buf(30, 2))
	subtree:add(fields.crc, buf(1020, 4))
	ds_lcd_subtree = subtree:add(fields.ds_lcd_f, buf(1024, 128))
	ds_lcd_subtree:add(fields.ds_lcd.command, buf(1024, 2))
	for i, l in ipairs(fields.ds_lcd.lines) do
		ds_lcd_subtree:add(l, buf(1005 + (21 * i), 21))
	end
end
 
-- Initialization routine
function frc_robot_2015.init()
end
 
-- register a chained dissector for port 1150
DissectorTable.get("udp.port"):add(1150, frc_robot_2015)
