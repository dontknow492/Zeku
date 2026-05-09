package com.ghost.zeku.data.repository

import com.ghost.zeku.data.local.room.DefaultCategories
import com.ghost.zeku.data.local.room.dao.LibraryCategoryDao
import com.ghost.zeku.data.local.room.entities.LibraryCategoryEntity
import com.ghost.zeku.data.local.room.item.CategoryWithCount
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.domain.repository.CategoryRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow

class CategoryRepositoryImpl(
    private val categoryDao: LibraryCategoryDao
) : CategoryRepository {

    private companion object {
        const val TAG = "CategoryRepo"
    }

    override fun observeAll(): Flow<List<LibraryCategoryEntity>> {
        Napier.d(tag = TAG) { "observeAll()" }
        return categoryDao.observeAll()
    }

    override fun observeVisible(mediaType: MediaType): Flow<List<LibraryCategoryEntity>> {
        Napier.d(tag = TAG) { "observeVisible(mediaType=$mediaType)" }
        return categoryDao.observeVisibleByType(mediaType)
    }

    override fun observeDefault(): Flow<List<LibraryCategoryEntity>> {
        Napier.d(tag = TAG) { "observeDefault()" }
        return categoryDao.observeDefault()
    }

    override fun observeWithItemCount(): Flow<List<CategoryWithCount>> {
        Napier.d(tag = TAG) { "observeWithItemCount()" }
        return categoryDao.observeCategoriesWithCount()
    }

    override suspend fun getById(id: Long): LibraryCategoryEntity? {
        Napier.d(tag = TAG) { "getById($id)" }
        return try {
            categoryDao.getById(id).also {
                Napier.d(tag = TAG) { "getById($id) -> $it" }
            }
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "getById($id) failed" }
            throw e
        }
    }

    override suspend fun getByName(name: String, type: MediaType): LibraryCategoryEntity? {
        Napier.d(tag = TAG) { "getByName(name=$name, type=$type)" }
        return try {
            categoryDao.getByName(name, type).also {
                Napier.d(tag = TAG) { "getByName -> $it" }
            }
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "getByName failed" }
            throw e
        }
    }

    override suspend fun findBestMatch(name: String, type: MediaType): LibraryCategoryEntity? {
        Napier.d(tag = TAG) { "findBestMatch(name=$name, type=$type)" }
        return try {
            categoryDao.findBestMatch(name, type).also {
                Napier.d(tag = TAG) { "findBestMatch -> $it" }
            }
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "findBestMatch failed" }
            throw e
        }
    }

    override suspend fun create(category: LibraryCategoryEntity): Long {
        Napier.d(tag = TAG) { "create(category=$category)" }
        return try {
            categoryDao.insert(category).also { id ->
                Napier.i(tag = TAG) { "Category created with id=$id" }
            }
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "create failed" }
            throw e
        }
    }

    override suspend fun update(category: LibraryCategoryEntity) {
        Napier.d(tag = TAG) { "update(category=${category.id})" }
        try {
            categoryDao.update(category)
            Napier.d(tag = TAG) { "update(${category.id}) success" }
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "update(${category.id}) failed" }
            throw e
        }
    }

    override suspend fun delete(categoryId: Long) {
        Napier.d(tag = TAG) { "delete($categoryId)" }
        try {
            categoryDao.deleteById(categoryId)
            Napier.i(tag = TAG) { "Category $categoryId deleted" }
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "delete($categoryId) failed" }
            throw e
        }
    }

    override suspend fun ensureDefaultsExist() {
        Napier.d(tag = TAG) { "ensureDefaultsExist()" }
        try {
            if (categoryDao.count() == 0) {
                Napier.i(tag = TAG) { "No categories found, inserting defaults" }
                categoryDao.insertAll(DefaultCategories.SYSTEM_CATEGORIES)
                Napier.i(tag = TAG) { "Default categories inserted" }
            } else {
                Napier.d(tag = TAG) { "Categories already exist, skipping defaults" }
            }
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "ensureDefaultsExist failed" }
            throw e
        }
    }

    override suspend fun setVisibility(id: Long, visible: Boolean) {
        Napier.d(tag = TAG) { "setVisibility(id=$id, visible=$visible)" }
        try {
            categoryDao.setVisibility(id, visible)
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "setVisibility failed" }
            throw e
        }
    }

    override suspend fun updateSortOrder(id: Long, order: Int) {
        Napier.d(tag = TAG) { "updateSortOrder(id=$id, order=$order)" }
        try {
            categoryDao.updateSortOrder(id, order)
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "updateSortOrder failed" }
            throw e
        }
    }

    override suspend fun count(): Int {
        Napier.d(tag = TAG) { "count()" }
        return try {
            categoryDao.count().also {
                Napier.d(tag = TAG) { "count() = $it" }
            }
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "count() failed" }
            throw e
        }
    }
}