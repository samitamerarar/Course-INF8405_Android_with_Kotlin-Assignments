package com.example.myapplication.utils


class BTInfoAnalyser {
    fun deviceType(code: Int): String {
        return when (code) {
            0 -> { "Unknown device type" }
            1 -> { "Classic - BR/EDR device" }
            2 -> { "Low Energy - LE-only device" }
            3 -> { "Dual Mode - BR/EDR/LE device" }
            else -> "Error reading device type"
        }
    }

    fun deviceBondState(code: Int): String {
        return when (code) {
            10 -> { "Not bonded (not paired)" }
            11 -> { "Bonding (pairing) is in progress" }
            12 -> { "Bonded (paired)" }
            else -> "Error reading pairing state"
        }
    }

    fun bluetoothDeviceClass(code: Int): String {
        // take only last 12 bits from the CoD (Class of Device) value
        return when (code and 0xfff) {
            1076 -> { "AUDIO_VIDEO_CAMCORDER" }
            1056 -> { "AUDIO_VIDEO_CAR_AUDIO" }
            1032 -> { "AUDIO_VIDEO_HANDSFREE" }
            1048 -> { "AUDIO_VIDEO_HEADPHONES" }
            1064 -> { "AUDIO_VIDEO_HIFI_AUDIO" }
            1044 -> { "AUDIO_VIDEO_LOUDSPEAKER" }
            1040 -> { "AUDIO_VIDEO_MICROPHONE" }
            1052 -> { "AUDIO_VIDEO_PORTABLE_AUDIO" }
            1060 -> { "AUDIO_VIDEO_SET_TOP_BOX" }
            1024 -> { "AUDIO_VIDEO_UNCATEGORIZED" }
            1068 -> { "AUDIO_VIDEO_VCR" }
            1072 -> { "AUDIO_VIDEO_VIDEO_CAMERA" }
            1088 -> { "AUDIO_VIDEO_VIDEO_CONFERENCING" }
            1084 -> { "AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER" }
            1096 -> { "AUDIO_VIDEO_VIDEO_GAMING_TOY" }
            1080 -> { "AUDIO_VIDEO_VIDEO_MONITOR" }
            1028 -> { "AUDIO_VIDEO_WEARABLE_HEADSET" }
            260 -> { "COMPUTER_DESKTOP" }
            272 -> { "COMPUTER_HANDHELD_PC_PDA" }
            268 -> { "COMPUTER_LAPTOP" }
            276 -> { "COMPUTER_PALM_SIZE_PC_PDA" }
            264 -> { "COMPUTER_SERVER" }
            256 -> { "COMPUTER_UNCATEGORIZED" }
            280 -> { "COMPUTER_WEARABLE" }
            2308 -> { "HEALTH_BLOOD_PRESSURE" }
            2332 -> { "HEALTH_DATA_DISPLAY" }
            2320 -> { "HEALTH_GLUCOSE" }
            2324 -> { "HEALTH_PULSE_OXIMETER" }
            2328 -> { "HEALTH_PULSE_RATE" }
            2312 -> { "HEALTH_THERMOMETER" }
            2304 -> { "HEALTH_UNCATEGORIZED" }
            2316 -> { "HEALTH_WEIGHING" }
            516 -> { "PHONE_CELLULAR" }
            520 -> { "PHONE_CORDLESS" }
            532 -> { "PHONE_ISDN" }
            528 -> { "PHONE_MODEM_OR_GATEWAY" }
            524 -> { "PHONE_SMART" }
            512 -> { "PHONE_UNCATEGORIZED" }
            2064 -> { "TOY_CONTROLLER" }
            2060 -> { "TOY_DOLL_ACTION_FIGURE" }
            2068 -> { "TOY_GAME" }
            2052 -> { "TOY_ROBOT" }
            2048 -> { "TOY_UNCATEGORIZED" }
            2056 -> { "TOY_VEHICLE" }
            1812 -> { "WEARABLE_GLASSES" }
            1808 -> { "WEARABLE_HELMET" }
            1804 -> { "WEARABLE_JACKET" }
            1800 -> { "WEARABLE_PAGER" }
            1792 -> { "WEARABLE_UNCATEGORIZED" }
            1796 -> { "WEARABLE_WRIST_WATCH" }
            else -> "Unknown Class of Device (CoD)"
        }
    }
}