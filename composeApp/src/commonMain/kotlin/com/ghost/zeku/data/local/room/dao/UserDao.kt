package com.ghost.zeku.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ghost.zeku.data.local.room.entities.UserEntity
import com.ghost.zeku.domain.model.ProviderType
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // Returns a Flow so our UI automatically updates whenever the DB changes!
    @Query("SELECT * FROM user_profiles WHERE providerType = :provider")
    fun getUserProfileFlow(provider: ProviderType): Flow<UserEntity?>

    @Query("SELECT * FROM user_profiles")
    fun getAllUserProfilesFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM user_profiles WHERE providerType = :provider")
    suspend fun deleteUser(provider: ProviderType)
}






