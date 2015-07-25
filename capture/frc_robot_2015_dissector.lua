frc_robot_2015 = Proto("frc_robot_2015","FRC 2015 Robot Protocol")

local MODE_VALS = {[0x0] = "Teleoperated", [0x2] = "Autonomous", [0x1] = "Test"}
local fields = {
	pkt_num = ProtoField.uint16("frc_robot_2015.pkt_num", "Packet Number", base.DEC),
	section_04_f = ProtoField.bytes("frc_robot_2015.section_04", "Section 0x04"),
	section_05_f = ProtoField.bytes("frc_robot_2015.section_05", "Section 0x05"),
	section_06_f = ProtoField.bytes("frc_robot_2015.section_06", "Section 0x06"),
	section_0e_f = ProtoField.bytes("frc_robot_2015.section_0e", "Section 0x0e"),
	flags_f = ProtoField.uint8("frc_robot_2015.flags", "Flags", base.HEX),
	flags = {
		mode = ProtoField.uint8("frc_robot_2015.flags.mode", "Mode", base.HEX, MODE_VALS, 0x3),
		enabled = ProtoField.bool("frc_robot_2015.flags.enabled", "Enabled", 8, nil, 0x4),
	},
        battery_voltage = ProtoField.bytes("frc_robot_2015.battery_voltage", "Battery Voltage") 
}

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

local function add_section_04(subtree, buf)
	subtree:add(fields.section_04_f, buf)
end

local function add_section_05(subtree, buf)
	subtree:add(fields.section_05_f, buf)
end

local function add_section_06(subtree, buf)
	subtree:add(fields.section_06_f, buf)
end

local function add_section_0e(subtree, buf)
	subtree:add(fields.section_0e_f, buf)
end

local function add_section(subtree, buf)
	local data_length_buf = buf(0, 1)
	local data_length = data_length_buf:uint() + 1
	local data_buf = buf(0, data_length)
	if data_length <= buf:len() then
		local section_type = buf(1, 1):uint()
		if section_type == 0x04 then
			add_section_04(subtree, data_buf)
		elseif section_type == 0x05 then
			add_section_05(subtree, data_buf)
		elseif section_type == 0x06 then
			add_section_06(subtree, data_buf)
		elseif section_type == 0x0e then
			add_section_0e(subtree, data_buf)
		end
	end
	return data_length
end

-- frc_robot_2015 dissector function
function frc_robot_2015.dissector (buf, pkt, root)
	pkt.cols.protocol = frc_robot_2015.name

	-- create subtree
	local subtree = root:add(frc_robot_2015, buf(0))
  	subtree:add(fields.pkt_num, buf(0, 2))

	local flags_buf = buf(3, 1)
	local flags_subtree = subtree:add(fields.flags_f, flags_buf)
	flags_subtree:add(fields.flags.mode, flags_buf)
	flags_subtree:add(fields.flags.enabled, flags_buf)

	local battery_voltage_buf = buf(5, 2)
        subtree:add(fields.battery_voltage, battery_voltage_buf):append_text(" ("..battery_voltage_buf(0, 1):uint().."."..battery_voltage_buf(1, 1):uint()..")")

	local pos = 8
	while buf(pos - 1):len() > 1 do
		pos = pos + add_section(subtree, buf(pos))
	end
end
 
-- Initialization routine
function frc_robot_2015.init()
end
 
-- register a chained dissector for port 1150
DissectorTable.get("udp.port"):add(1150, frc_robot_2015)
