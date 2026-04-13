package com.clipboardreminder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.clipboardreminder.data.local.entity.FieldEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FieldDao {

    @Query("SELECT * FROM fields ORDER BY isPinned DESC, name ASC")
    fun getOrderedFields(): Flow<List<FieldEntity>>

    @Query("SELECT * FROM fields WHERE id = :id")
    fun getFieldById(id: Long): Flow<FieldEntity?>

    @Insert
    suspend fun insert(field: FieldEntity): Long

    @Update
    suspend fun update(field: FieldEntity)

    @Delete
    suspend fun delete(field: FieldEntity)

    @Transaction
    @Query("DELETE FROM fields WHERE id = :id")
    suspend fun deleteById(id: Long)
}
