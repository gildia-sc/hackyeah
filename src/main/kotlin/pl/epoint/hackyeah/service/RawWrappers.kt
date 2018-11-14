package pl.epoint.hackyeah.service

open class StringWrapper(val raw: String)

open class LongWrapper(val raw: Long)

class FoosballTableCode(value: String) : StringWrapper(value)

class TeamColor(value: String) : StringWrapper(value)
