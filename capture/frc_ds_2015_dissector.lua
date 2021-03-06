frc_ds_2015 = Proto ("frc_ds_2015","FRC 2015 Driver Station Protocol")

local MODE_VALS = {[0x0] = "Teleoperated", [0x2] = "Autonomous", [0x1] = "Test"}
local SECTION_TYPE_VALS = {[0x0c] = "Joystick", [0x0f] = "Time", [0x10] = "Timezone"}
local POSITION_VALS = {[0x0] = "Red 1", [0x01] = "Red 2", [0x02] = "Red 3", [0x03] = "Blue 1", [0x04] = "Blue 2", [0x05] = "Blue 3"}
local fields = {
	pkt_num = ProtoField.uint16("frc_ds_2015.pkt_num", "Packet Number", base.DEC),
	flags_f = ProtoField.uint8("frc_ds_2015.flags", "Flags", base.HEX),
	flags = {
		mode = ProtoField.uint8("frc_ds_2015.flags.mode", "Mode", base.HEX, MODE_VALS, 0x3),
		enabled = ProtoField.bool("frc_ds_2015.flags.enabled", "Enabled", 8, nil, 0x4),
		emergency_stopped = ProtoField.bool("frc_ds_2015.flags.emergency_stopped", "Emergency Stopped", 8, nil, 0x80)
	},
	position = ProtoField.uint8("frc_ds_2015.position", "Position", base.HEX, POSITION_VALS),
	joystick = {},
	time_f = ProtoField.bytes("frc_ds_2015.time", "Time"),
	time = {
		data_length = ProtoField.uint8("frc_ds_2015.time.data_length", "Data Length", base.DEC),
		section_type = ProtoField.uint8("frc_ds_2015.time.section_type", "Section Type", base.HEX, SECTION_TYPE_VALS),
		second = ProtoField.uint8("frc_ds_2015.time.second", "Second", base.DEC),
		minute = ProtoField.uint8("frc_ds_2015.time.minute", "Minute", base.DEC),
		hour = ProtoField.uint8("frc_ds_2015.time.hour", "Hour", base.DEC),
		day = ProtoField.uint8("frc_ds_2015.time.day", "Day", base.DEC),
		month = ProtoField.uint8("frc_ds_2015.time.month", "Month", base.DEC),
		year = ProtoField.uint8("frc_ds_2015.time.year", "Year", base.DEC)
	},
	timezone_f = ProtoField.bytes("frc_ds_2015.timezone", "Timezone"),
	timezone = {
		data_length = ProtoField.uint8("frc_ds_2015.timezone.data_length", "Data Length", base.DEC),
		section_type = ProtoField.uint8("frc_ds_2015.timezone.section_type", "Section Type", base.HEX, SECTION_TYPE_VALS),
		name = ProtoField.string("frc_ds_2015.timezone.name", "Name"),
	}
}

for i = 1, 4 do
	table.insert(fields.joystick, {
		field = ProtoField.bytes("frc_ds_2015.joystick."..i, "Joystick "..i),
		data_length = ProtoField.uint8("frc_ds_2015.joystick."..i..".data_length", "Data Length", base.DEC),
		section_type = ProtoField.uint8("frc_ds_2015.joystick."..i..".section_type", "Section Type", base.HEX, SECTION_TYPE_VALS),
		axis_count = ProtoField.uint8("frc_ds_2015.joystick."..i..".axis_count", "Axis Count", base.DEC),
		axis_f = ProtoField.bytes("frc_ds_2015.joystick."..i..".axis", "Axes"),
		axis = {},
		button_count = ProtoField.int8("frc_ds_2015.joystick."..i..".button_count", "Button Count", base.DEC),
		button_f = ProtoField.uint8("frc_ds_2015.joystick."..i..".button", "Buttons", base.HEX),
		button = {},
		pov_hat_count = ProtoField.uint8("frc_ds_2015.joystick."..i..".pov_hat_count", "POV Hat Count", base.DEC),
		pov_hat_f = ProtoField.bytes("frc_ds_2015.joystick."..i..".pov_hat", "POV Hats"),
		pov_hat = {}
	})
	for a = 1, 6 do
		table.insert(fields.joystick[i].axis, ProtoField.int8("frc_ds_2015.joystick."..i..".axis."..a, "Axis "..a, base.DEC))
	end
	local mask = 0x1
	for b = 1, 12 do
		table.insert(fields.joystick[i].button, ProtoField.bool("frc_ds_2015.joystick."..i..".button."..b, "Button "..b, 16, nil, mask))
		mask = mask * 2
	end
	for h = 1, 4 do
		table.insert(fields.joystick[i].pov_hat, ProtoField.int16("frc_ds_2015.joystick."..i..".pov_hat."..h, "Hat "..h, base.DEC))
	end
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

frc_ds_2015.fields = flatten(fields)

local function mod(a, b)
	return a - math.floor(a/b)*b
end

-- Get a month name from its number
local function get_month(month)
	local months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" }
	return months[tonumber(month) + 1]
end

local function add_joystick(num, subtree, buf)
	local joystick_field = fields.joystick[num]
	local data_length_buf = buf(0, 1)
	local data_length = data_length_buf:uint() + 1
	local joystick_subtree = subtree:add(joystick_field.field, buf(0, data_length))
	joystick_subtree:add(joystick_field.data_length, data_length_buf)
	joystick_subtree:add(joystick_field.section_type, buf(1, 1))
	local axis_count_buf = buf(2, 1)
	local axis_count = axis_count_buf:uint()
	joystick_subtree:add(joystick_field.axis_count, axis_count_buf)
	if axis_count > 0 then
		local real_axis_count = math.min(#joystick_field.axis, axis_count)
		joystick_axis_subtree = joystick_subtree:add(joystick_field.axis_f, buf(0x3, real_axis_count))
		for a = 1, real_axis_count do
			joystick_axis_subtree:add(joystick_field.axis[a], buf(a + 0x2, 1))
		end
	end
	-- Button count field
	local button_count_buf = buf(0x3 + axis_count, 1)
	local button_count = button_count_buf:uint()
	joystick_subtree:add(joystick_field.button_count, button_count_buf)

	-- Buttons
	local button_start = 0x4 + axis_count
	local button_byte_count = math.ceil(button_count / 8)
	if button_count > 0 then
		local button_buf = buf(button_start, button_byte_count)
		local joystick_button_subtree = joystick_subtree:add(joystick_field.button_f, button_buf)
		for b = 1, math.min(#joystick_field.button, button_count) do
			joystick_button_subtree:add(joystick_field.button[b], button_buf)
		end
	end

	-- POV hat count field
	local pov_hat_start = button_start + button_byte_count
	local pov_hat_count_buf = buf(pov_hat_start, 1)
	local pov_hat_count = pov_hat_count_buf:uint()
	joystick_subtree:add(joystick_field.pov_hat_count, pov_hat_count_buf)

	-- POV hats
	if pov_hat_count > 0 then
		local real_pov_hat_count = math.min(#joystick_field.pov_hat, pov_hat_count)
		pov_hat_subtree = joystick_subtree:add(joystick_field.pov_hat_f, buf(pov_hat_start + 1, real_pov_hat_count * 2))
		for h = 0, real_pov_hat_count - 1 do
			local pov_hat_buf = buf(pov_hat_start + 1 + (2 * h), 2)
			local pov_hat_field = pov_hat_subtree:add(joystick_field.pov_hat[h + 1], pov_hat_buf)
			if pov_hat_buf:int() == -1 then
				pov_hat_field:append_text(" (Not Pressed)")
			end
		end
	end
end

local function add_time(subtree, buf)
	local data_length_buf = buf(0, 1)
	if data_length_buf:uint() ~= 11 then end
	local second_buf = buf(6 , 1)
	local second = second_buf:uint()
	local minute_buf = buf(7, 1)
	local minute = minute_buf:uint()
	local hour_buf = buf(8, 1)
	local hour = hour_buf:uint()
	local day_buf = buf(9, 1)
	local day = day_buf:uint()
	local month_buf = buf(10, 1)
	local month = month_buf:uint()
	local month_text = get_month(month)
	local year_buf = buf(11, 1)
	local year = year_buf:uint() + 1900
	local time_subtree = subtree:add(fields.time_f, buf)
	time_subtree:append_text(" ("..month_text.." "..day.." "..hour..":"..minute..":"..second.." "..year..")")
	time_subtree:add(fields.time.data_length, data_length_buf)
	time_subtree:add(fields.time.section_type, buf(1, 1))
	time_subtree:add(fields.time.second, second_buf)
	time_subtree:add(fields.time.minute, minute_buf)
	time_subtree:add(fields.time.hour, hour_buf)
	time_subtree:add(fields.time.day, day_buf)
	time_subtree:add(fields.time.month, month_buf):append_text(" ("..month_text..")")
	time_subtree:add(fields.time.year, year_buf, year)
end

local function add_timezone(subtree, buf)
	timezone_subtree = subtree:add(fields.timezone_f, buf)
	local name_buf = buf(2)
	local name = name_buf:string()
	timezone_subtree:append_text(" ("..name..")")
	timezone_subtree:add(fields.timezone.data_length, buf(0, 1))
	timezone_subtree:add(fields.timezone.section_type, buf(1, 1))
	timezone_subtree:add(fields.timezone.name, name_buf)
end

local function add_section(subtree, buf)
	local data_length_buf = buf(0, 1)
	local data_length = data_length_buf:uint() + 1
	local data_buf = buf(0, data_length)
	if data_length <= buf:len() then
		local section_type = buf(1, 1):uint()
		if section_type == 0x0c then
			if joystick_num < #fields.joystick then
				joystick_num = joystick_num + 1
				add_joystick(joystick_num, subtree, data_buf)
			end
		elseif section_type == 0x0f then
			add_time(subtree, data_buf)
		elseif section_type == 0x10 then
			add_timezone(subtree, data_buf)
		end
	end
	return data_length
end

-- frc_ds_2015 dissector function
function frc_ds_2015.dissector (buf, pkt, root)
	pkt.cols.protocol = frc_ds_2015.name

	-- create subtree
	subtree = root:add(frc_ds_2015, buf(0))

  	subtree:add(fields.pkt_num, buf(0, 2))
	local flags_buf = buf(3, 1)
	local flags_subtree = subtree:add(fields.flags_f, flags_buf)
	flags_subtree:add(fields.flags.mode, flags_buf)
	flags_subtree:add(fields.flags.enabled, flags_buf)
	flags_subtree:add(fields.flags.emergency_stopped, flags_buf)

	subtree:add(fields.position, buf(5, 1))

	local pos = 6
	joystick_num = 0
	while buf(pos - 1):len() > 1 do
		pos = pos + add_section(subtree, buf(pos))
	end
end
 
-- Initialization routine
function frc_ds_2015.init()
end
 
-- register a chained dissector for port 1110
DissectorTable.get("udp.port"):add(1110, frc_ds_2015)
