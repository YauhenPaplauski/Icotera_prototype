package by.softteco.icotera_test.utils

internal object Services {
    val icoteraApi: IcoteraApi by lazy { IcoteraApiImpl() }
}