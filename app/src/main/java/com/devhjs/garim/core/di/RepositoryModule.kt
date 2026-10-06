package com.devhjs.garim.core.di
import com.devhjs.garim.data.datasource.local.*
import com.devhjs.garim.data.repository.PhotoRepositoryImpl
import com.devhjs.garim.domain.repository.PhotoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindPhotoRepository(impl: PhotoRepositoryImpl): PhotoRepository
    @Binds @Singleton abstract fun bindPhotoLocalDataSource(impl: AndroidPhotoLocalDataSource): PhotoLocalDataSource
}
