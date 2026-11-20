package sa.factory.masarrtknavigation.di

import sa.factory.masarrtknavigation.data.repository.MasarRtkRepositoryImpl
import sa.factory.masarrtknavigation.domain.repository.MasarRtkRepository
import sa.factory.masarrtknavigation.domain.usecase.*
import sa.factory.masarrtknavigation.hardware.HardwareSensorDriver
import sa.factory.masarrtknavigation.ui.viewmodel.MasarRtkViewModel

class DiContainer {
    val sensorDriver = HardwareSensorDriver()
    val repository: MasarRtkRepository = MasarRtkRepositoryImpl(sensorDriver)

    val getCatalogUseCase = GetCatalogUseCase(repository)
    val manageCartUseCase = ManageCartUseCase(repository)
    val processCheckoutUseCase = ProcessCheckoutUseCase(repository)
    val observeHardwareTelemetryUseCase = ObserveHardwareTelemetryUseCase(repository)

    fun provideViewModel(): MasarRtkViewModel = MasarRtkViewModel(
        getCatalogUseCase,
        manageCartUseCase,
        processCheckoutUseCase,
        observeHardwareTelemetryUseCase
    )
}
