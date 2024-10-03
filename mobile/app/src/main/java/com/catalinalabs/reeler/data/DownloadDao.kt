package com.catalinalabs.reeler.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.catalinalabs.reeler.network.models.VideoInfoOutput
import kotlinx.coroutines.flow.Flow

@Database(entities = [DownloadEntity::class], version = 2, exportSchema = false)
abstract class ReelerDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao

    companion object {
        @Volatile
        private var Instance: ReelerDatabase? = null

        fun getDatabase(context: Context): ReelerDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, ReelerDatabase::class.java, "reeler_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}

@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(videoInfo: DownloadEntity)

    @Update
    suspend fun update(videoInfo: DownloadEntity)

    @Delete
    suspend fun delete(videoInfo: DownloadEntity)

    @Query("SELECT * FROM downloads WHERE id = :id")
    fun getOne(id: Int): Flow<DownloadEntity>

    @Query("SELECT * FROM downloads ORDER BY id DESC")
    fun getAll(): Flow<List<DownloadEntity>>
}

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val filename: String = "",
    val contentUrl: String = "",
    val sourceUrl: String = "",
    val source: String = "",
    val width: Int? = null,
    val height: Int? = null,
    val username: String? = null,
    val caption: String? = null,
    val duration: Double? = null,
    val userAvatarUrl: String? = null,
    val thumbnailUrl: String? = null,
    val mediaUri: String? = null,
    val timestamp: Long? = null,
    val size: Long = 0,
)

fun DownloadEntity.asVideoInfoOutput(): VideoInfoOutput {
    return VideoInfoOutput(
        filename = this.filename,
        contentUrl = this.contentUrl,
        sourceUrl = this.sourceUrl,
        source = this.source,
        width = this.width,
        height = this.height,
        username = this.username,
        caption = this.caption,
        duration = this.duration,
        userAvatarUrl = this.userAvatarUrl,
        thumbnailUrl = this.thumbnailUrl,
    )
}

object DownloadMockData {
    val forPreview: List<DownloadEntity> = listOf(
        DownloadEntity(
            id = 1,
            filename = "ig-downloader-1727597487.mp4",
            contentUrl = "https://instagram.alfosuag.workers.dev/video-download/aHR0cHM6Ly9zY29udGVudC15eXoxLTEuY2RuaW5zdGFncmFtLmNvbS9vMS92L3QxNi9mMS9tODIvRUY0MjE3NTkyQ0IyMDBEM0U2QTAzMDIyMzM0M0E5QUNfdmlkZW9fZGFzaGluaXQubXA0P3N0cD1kc3QtbXA0JmVmZz1leUp4WlY5bmNtOTFjSE1pT2lKYlhDSnBaMTkzWldKZlpHVnNhWFpsY25sZmRuUnpYMjkwWmx3aVhTSXNJblpsYm1OdlpHVmZkR0ZuSWpvaWRuUnpYM1p2WkY5MWNteG5aVzR1WTJ4cGNITXVZekl1TnpJd0xtSmhjMlZzYVc1bEluMCZfbmNfY2F0PTExMCZ2cz04MTA3NTA1OTA5MTQ0ODRfMjYzMDQyMzQ1OCZfbmNfdnM9SEJrc0ZRSVlUMmxuWDNod2RsOXlaV1ZzYzE5d1pYSnRZVzVsYm5SZmNISnZaQzlGUmpReU1UYzFPVEpEUWpJd01FUXpSVFpCTURNd01qSXpNelF6UVRsQlExOTJhV1JsYjE5a1lYTm9hVzVwZEM1dGNEUVZBQUxJQVFBVkFoZzZjR0Z6YzNSb2NtOTFaMmhmWlhabGNuTjBiM0psTDBkSExYSldhSEl0YTNCeFVYZ3lhMDVCUjFOd2ExVlJSR2hYU1ZsaWNWOUZRVUZCUmhVQ0FzZ0JBQ2dBR0FBYkFCVUFBQ2FXblBHUHhlT0ZRUlVDS0FKRE15d1hRRE16TXpNek16TVlFbVJoYzJoZlltRnpaV3hwYm1WZk1WOTJNUkVBZGY0SEFBJTNEJTNEJl9uY19yaWQ9YjI2MGVjMGE4MyZjY2I9OS00Jm9oPTAwX0FZRGo1eVV5MGZHdThrMm4zbmhVQlVLMTBoS3MxcGFrZkJkM2thSG1nRHJUdkEmb2U9NjZGQUVEMjQmX25jX3NpZD0xMGQxM2I=.mp4",
            width = 750,
            height = 1333,
            username = "cris_villegas07",
            caption = "Part 1 | Jujutsu Kaisen 222-235 manga chapters Animation 🔥",
            duration = 19.2,
            userAvatarUrl = "https://scontent-yyz1-1.cdninstagram.com/v/t51.2885-19/457621679_877143554292925_4043951588544897022_n.jpg?stp=dst-jpg_e0_s150x150&_nc_ht=scontent-yyz1-1.cdninstagram.com&_nc_cat=101&_nc_ohc=BsuRG3nmJBYQ7kNvgHKLVtG&_nc_gid=b260e280f8ee4b079f61b4c67b4b2c92&edm=APs17CUBAAAA&ccb=7-5&oh=00_AYBkT_2-MZ4SwYe5vD9FCmwdMLvxhJT03wn43vDi9cDFmA&oe=66FEEF3C&_nc_sid=10d13b",
            thumbnailUrl = "https://scontent-yyz1-1.cdninstagram.com/v/t51.29350-15/441015281_460146716487560_3290678782543390784_n.jpg?stp=c0.280.720.720a_dst-jpg_e15_s640x640&_nc_ht=scontent-yyz1-1.cdninstagram.com&_nc_cat=108&_nc_ohc=6VUypcyjz54Q7kNvgHCfifs&_nc_gid=b260e280f8ee4b079f61b4c67b4b2c92&edm=APs17CUBAAAA&ccb=7-5&oh=00_AYBSTuioxdGQnAKVeFPWaHcr9efa18de73IH0RDqQqrmkg&oe=66FEDE70&_nc_sid=10d13b",
            timestamp = null,
            mediaUri = "",
        ),
        DownloadEntity(
            id = 1,
            filename = "ig-downloader-1727597487.mp4",
            contentUrl = "https://instagram.alfosuag.workers.dev/video-download/aHR0cHM6Ly9zY29udGVudC15eXoxLTEuY2RuaW5zdGFncmFtLmNvbS9vMS92L3QxNi9mMS9tODIvRUY0MjE3NTkyQ0IyMDBEM0U2QTAzMDIyMzM0M0E5QUNfdmlkZW9fZGFzaGluaXQubXA0P3N0cD1kc3QtbXA0JmVmZz1leUp4WlY5bmNtOTFjSE1pT2lKYlhDSnBaMTkzWldKZlpHVnNhWFpsY25sZmRuUnpYMjkwWmx3aVhTSXNJblpsYm1OdlpHVmZkR0ZuSWpvaWRuUnpYM1p2WkY5MWNteG5aVzR1WTJ4cGNITXVZekl1TnpJd0xtSmhjMlZzYVc1bEluMCZfbmNfY2F0PTExMCZ2cz04MTA3NTA1OTA5MTQ0ODRfMjYzMDQyMzQ1OCZfbmNfdnM9SEJrc0ZRSVlUMmxuWDNod2RsOXlaV1ZzYzE5d1pYSnRZVzVsYm5SZmNISnZaQzlGUmpReU1UYzFPVEpEUWpJd01FUXpSVFpCTURNd01qSXpNelF6UVRsQlExOTJhV1JsYjE5a1lYTm9hVzVwZEM1dGNEUVZBQUxJQVFBVkFoZzZjR0Z6YzNSb2NtOTFaMmhmWlhabGNuTjBiM0psTDBkSExYSldhSEl0YTNCeFVYZ3lhMDVCUjFOd2ExVlJSR2hYU1ZsaWNWOUZRVUZCUmhVQ0FzZ0JBQ2dBR0FBYkFCVUFBQ2FXblBHUHhlT0ZRUlVDS0FKRE15d1hRRE16TXpNek16TVlFbVJoYzJoZlltRnpaV3hwYm1WZk1WOTJNUkVBZGY0SEFBJTNEJTNEJl9uY19yaWQ9YjI2MGVjMGE4MyZjY2I9OS00Jm9oPTAwX0FZRGo1eVV5MGZHdThrMm4zbmhVQlVLMTBoS3MxcGFrZkJkM2thSG1nRHJUdkEmb2U9NjZGQUVEMjQmX25jX3NpZD0xMGQxM2I=.mp4",
            width = 750,
            height = 1333,
            username = "cris_villegas07",
            caption = "Part 1 | Jujutsu Kaisen 222-235 manga chapters Animation 🔥",
            duration = 19.2,
            userAvatarUrl = "https://scontent-yyz1-1.cdninstagram.com/v/t51.2885-19/457621679_877143554292925_4043951588544897022_n.jpg?stp=dst-jpg_e0_s150x150&_nc_ht=scontent-yyz1-1.cdninstagram.com&_nc_cat=101&_nc_ohc=BsuRG3nmJBYQ7kNvgHKLVtG&_nc_gid=b260e280f8ee4b079f61b4c67b4b2c92&edm=APs17CUBAAAA&ccb=7-5&oh=00_AYBkT_2-MZ4SwYe5vD9FCmwdMLvxhJT03wn43vDi9cDFmA&oe=66FEEF3C&_nc_sid=10d13b",
            thumbnailUrl = "https://scontent-yyz1-1.cdninstagram.com/v/t51.29350-15/441015281_460146716487560_3290678782543390784_n.jpg?stp=c0.280.720.720a_dst-jpg_e15_s640x640&_nc_ht=scontent-yyz1-1.cdninstagram.com&_nc_cat=108&_nc_ohc=6VUypcyjz54Q7kNvgHCfifs&_nc_gid=b260e280f8ee4b079f61b4c67b4b2c92&edm=APs17CUBAAAA&ccb=7-5&oh=00_AYBSTuioxdGQnAKVeFPWaHcr9efa18de73IH0RDqQqrmkg&oe=66FEDE70&_nc_sid=10d13b",
            timestamp = null,
            mediaUri = "",
        ),
        DownloadEntity(
            id = 1,
            filename = "ig-downloader-1727597487.mp4",
            contentUrl = "https://instagram.alfosuag.workers.dev/video-download/aHR0cHM6Ly9zY29udGVudC15eXoxLTEuY2RuaW5zdGFncmFtLmNvbS9vMS92L3QxNi9mMS9tODIvRUY0MjE3NTkyQ0IyMDBEM0U2QTAzMDIyMzM0M0E5QUNfdmlkZW9fZGFzaGluaXQubXA0P3N0cD1kc3QtbXA0JmVmZz1leUp4WlY5bmNtOTFjSE1pT2lKYlhDSnBaMTkzWldKZlpHVnNhWFpsY25sZmRuUnpYMjkwWmx3aVhTSXNJblpsYm1OdlpHVmZkR0ZuSWpvaWRuUnpYM1p2WkY5MWNteG5aVzR1WTJ4cGNITXVZekl1TnpJd0xtSmhjMlZzYVc1bEluMCZfbmNfY2F0PTExMCZ2cz04MTA3NTA1OTA5MTQ0ODRfMjYzMDQyMzQ1OCZfbmNfdnM9SEJrc0ZRSVlUMmxuWDNod2RsOXlaV1ZzYzE5d1pYSnRZVzVsYm5SZmNISnZaQzlGUmpReU1UYzFPVEpEUWpJd01FUXpSVFpCTURNd01qSXpNelF6UVRsQlExOTJhV1JsYjE5a1lYTm9hVzVwZEM1dGNEUVZBQUxJQVFBVkFoZzZjR0Z6YzNSb2NtOTFaMmhmWlhabGNuTjBiM0psTDBkSExYSldhSEl0YTNCeFVYZ3lhMDVCUjFOd2ExVlJSR2hYU1ZsaWNWOUZRVUZCUmhVQ0FzZ0JBQ2dBR0FBYkFCVUFBQ2FXblBHUHhlT0ZRUlVDS0FKRE15d1hRRE16TXpNek16TVlFbVJoYzJoZlltRnpaV3hwYm1WZk1WOTJNUkVBZGY0SEFBJTNEJTNEJl9uY19yaWQ9YjI2MGVjMGE4MyZjY2I9OS00Jm9oPTAwX0FZRGo1eVV5MGZHdThrMm4zbmhVQlVLMTBoS3MxcGFrZkJkM2thSG1nRHJUdkEmb2U9NjZGQUVEMjQmX25jX3NpZD0xMGQxM2I=.mp4",
            width = 750,
            height = 1333,
            username = "cris_villegas07",
            caption = "Part 1 | Jujutsu Kaisen 222-235 manga chapters Animation 🔥",
            duration = 19.2,
            userAvatarUrl = "https://scontent-yyz1-1.cdninstagram.com/v/t51.2885-19/457621679_877143554292925_4043951588544897022_n.jpg?stp=dst-jpg_e0_s150x150&_nc_ht=scontent-yyz1-1.cdninstagram.com&_nc_cat=101&_nc_ohc=BsuRG3nmJBYQ7kNvgHKLVtG&_nc_gid=b260e280f8ee4b079f61b4c67b4b2c92&edm=APs17CUBAAAA&ccb=7-5&oh=00_AYBkT_2-MZ4SwYe5vD9FCmwdMLvxhJT03wn43vDi9cDFmA&oe=66FEEF3C&_nc_sid=10d13b",
            thumbnailUrl = "https://scontent-yyz1-1.cdninstagram.com/v/t51.29350-15/441015281_460146716487560_3290678782543390784_n.jpg?stp=c0.280.720.720a_dst-jpg_e15_s640x640&_nc_ht=scontent-yyz1-1.cdninstagram.com&_nc_cat=108&_nc_ohc=6VUypcyjz54Q7kNvgHCfifs&_nc_gid=b260e280f8ee4b079f61b4c67b4b2c92&edm=APs17CUBAAAA&ccb=7-5&oh=00_AYBSTuioxdGQnAKVeFPWaHcr9efa18de73IH0RDqQqrmkg&oe=66FEDE70&_nc_sid=10d13b",
            timestamp = null,
            mediaUri = "",
        ),
        DownloadEntity(
            id = 1,
            filename = "ig-downloader-1727597487.mp4",
            contentUrl = "https://instagram.alfosuag.workers.dev/video-download/aHR0cHM6Ly9zY29udGVudC15eXoxLTEuY2RuaW5zdGFncmFtLmNvbS9vMS92L3QxNi9mMS9tODIvRUY0MjE3NTkyQ0IyMDBEM0U2QTAzMDIyMzM0M0E5QUNfdmlkZW9fZGFzaGluaXQubXA0P3N0cD1kc3QtbXA0JmVmZz1leUp4WlY5bmNtOTFjSE1pT2lKYlhDSnBaMTkzWldKZlpHVnNhWFpsY25sZmRuUnpYMjkwWmx3aVhTSXNJblpsYm1OdlpHVmZkR0ZuSWpvaWRuUnpYM1p2WkY5MWNteG5aVzR1WTJ4cGNITXVZekl1TnpJd0xtSmhjMlZzYVc1bEluMCZfbmNfY2F0PTExMCZ2cz04MTA3NTA1OTA5MTQ0ODRfMjYzMDQyMzQ1OCZfbmNfdnM9SEJrc0ZRSVlUMmxuWDNod2RsOXlaV1ZzYzE5d1pYSnRZVzVsYm5SZmNISnZaQzlGUmpReU1UYzFPVEpEUWpJd01FUXpSVFpCTURNd01qSXpNelF6UVRsQlExOTJhV1JsYjE5a1lYTm9hVzVwZEM1dGNEUVZBQUxJQVFBVkFoZzZjR0Z6YzNSb2NtOTFaMmhmWlhabGNuTjBiM0psTDBkSExYSldhSEl0YTNCeFVYZ3lhMDVCUjFOd2ExVlJSR2hYU1ZsaWNWOUZRVUZCUmhVQ0FzZ0JBQ2dBR0FBYkFCVUFBQ2FXblBHUHhlT0ZRUlVDS0FKRE15d1hRRE16TXpNek16TVlFbVJoYzJoZlltRnpaV3hwYm1WZk1WOTJNUkVBZGY0SEFBJTNEJTNEJl9uY19yaWQ9YjI2MGVjMGE4MyZjY2I9OS00Jm9oPTAwX0FZRGo1eVV5MGZHdThrMm4zbmhVQlVLMTBoS3MxcGFrZkJkM2thSG1nRHJUdkEmb2U9NjZGQUVEMjQmX25jX3NpZD0xMGQxM2I=.mp4",
            width = 750,
            height = 1333,
            username = "cris_villegas07",
            caption = "Part 1 | Jujutsu Kaisen 222-235 manga chapters Animation 🔥",
            duration = 19.2,
            userAvatarUrl = "https://scontent-yyz1-1.cdninstagram.com/v/t51.2885-19/457621679_877143554292925_4043951588544897022_n.jpg?stp=dst-jpg_e0_s150x150&_nc_ht=scontent-yyz1-1.cdninstagram.com&_nc_cat=101&_nc_ohc=BsuRG3nmJBYQ7kNvgHKLVtG&_nc_gid=b260e280f8ee4b079f61b4c67b4b2c92&edm=APs17CUBAAAA&ccb=7-5&oh=00_AYBkT_2-MZ4SwYe5vD9FCmwdMLvxhJT03wn43vDi9cDFmA&oe=66FEEF3C&_nc_sid=10d13b",
            thumbnailUrl = "https://scontent-yyz1-1.cdninstagram.com/v/t51.29350-15/441015281_460146716487560_3290678782543390784_n.jpg?stp=c0.280.720.720a_dst-jpg_e15_s640x640&_nc_ht=scontent-yyz1-1.cdninstagram.com&_nc_cat=108&_nc_ohc=6VUypcyjz54Q7kNvgHCfifs&_nc_gid=b260e280f8ee4b079f61b4c67b4b2c92&edm=APs17CUBAAAA&ccb=7-5&oh=00_AYBSTuioxdGQnAKVeFPWaHcr9efa18de73IH0RDqQqrmkg&oe=66FEDE70&_nc_sid=10d13b",
            timestamp = null,
            mediaUri = "",
        ),
        DownloadEntity(
            id = 1,
            filename = "ig-downloader-1727597487.mp4",
            contentUrl = "https://instagram.alfosuag.workers.dev/video-download/aHR0cHM6Ly9zY29udGVudC15eXoxLTEuY2RuaW5zdGFncmFtLmNvbS9vMS92L3QxNi9mMS9tODIvRUY0MjE3NTkyQ0IyMDBEM0U2QTAzMDIyMzM0M0E5QUNfdmlkZW9fZGFzaGluaXQubXA0P3N0cD1kc3QtbXA0JmVmZz1leUp4WlY5bmNtOTFjSE1pT2lKYlhDSnBaMTkzWldKZlpHVnNhWFpsY25sZmRuUnpYMjkwWmx3aVhTSXNJblpsYm1OdlpHVmZkR0ZuSWpvaWRuUnpYM1p2WkY5MWNteG5aVzR1WTJ4cGNITXVZekl1TnpJd0xtSmhjMlZzYVc1bEluMCZfbmNfY2F0PTExMCZ2cz04MTA3NTA1OTA5MTQ0ODRfMjYzMDQyMzQ1OCZfbmNfdnM9SEJrc0ZRSVlUMmxuWDNod2RsOXlaV1ZzYzE5d1pYSnRZVzVsYm5SZmNISnZaQzlGUmpReU1UYzFPVEpEUWpJd01FUXpSVFpCTURNd01qSXpNelF6UVRsQlExOTJhV1JsYjE5a1lYTm9hVzVwZEM1dGNEUVZBQUxJQVFBVkFoZzZjR0Z6YzNSb2NtOTFaMmhmWlhabGNuTjBiM0psTDBkSExYSldhSEl0YTNCeFVYZ3lhMDVCUjFOd2ExVlJSR2hYU1ZsaWNWOUZRVUZCUmhVQ0FzZ0JBQ2dBR0FBYkFCVUFBQ2FXblBHUHhlT0ZRUlVDS0FKRE15d1hRRE16TXpNek16TVlFbVJoYzJoZlltRnpaV3hwYm1WZk1WOTJNUkVBZGY0SEFBJTNEJTNEJl9uY19yaWQ9YjI2MGVjMGE4MyZjY2I9OS00Jm9oPTAwX0FZRGo1eVV5MGZHdThrMm4zbmhVQlVLMTBoS3MxcGFrZkJkM2thSG1nRHJUdkEmb2U9NjZGQUVEMjQmX25jX3NpZD0xMGQxM2I=.mp4",
            width = 750,
            height = 1333,
            username = "cris_villegas07",
            caption = "Part 1 | Jujutsu Kaisen 222-235 manga chapters Animation 🔥",
            duration = 19.2,
            userAvatarUrl = "https://scontent-yyz1-1.cdninstagram.com/v/t51.2885-19/457621679_877143554292925_4043951588544897022_n.jpg?stp=dst-jpg_e0_s150x150&_nc_ht=scontent-yyz1-1.cdninstagram.com&_nc_cat=101&_nc_ohc=BsuRG3nmJBYQ7kNvgHKLVtG&_nc_gid=b260e280f8ee4b079f61b4c67b4b2c92&edm=APs17CUBAAAA&ccb=7-5&oh=00_AYBkT_2-MZ4SwYe5vD9FCmwdMLvxhJT03wn43vDi9cDFmA&oe=66FEEF3C&_nc_sid=10d13b",
            thumbnailUrl = "https://scontent-yyz1-1.cdninstagram.com/v/t51.29350-15/441015281_460146716487560_3290678782543390784_n.jpg?stp=c0.280.720.720a_dst-jpg_e15_s640x640&_nc_ht=scontent-yyz1-1.cdninstagram.com&_nc_cat=108&_nc_ohc=6VUypcyjz54Q7kNvgHCfifs&_nc_gid=b260e280f8ee4b079f61b4c67b4b2c92&edm=APs17CUBAAAA&ccb=7-5&oh=00_AYBSTuioxdGQnAKVeFPWaHcr9efa18de73IH0RDqQqrmkg&oe=66FEDE70&_nc_sid=10d13b",
            timestamp = null,
            mediaUri = "",
        ),
        DownloadEntity(
            id = 1,
            filename = "ig-downloader-1727597487.mp4",
            contentUrl = "https://instagram.alfosuag.workers.dev/video-download/aHR0cHM6Ly9zY29udGVudC15eXoxLTEuY2RuaW5zdGFncmFtLmNvbS9vMS92L3QxNi9mMS9tODIvRUY0MjE3NTkyQ0IyMDBEM0U2QTAzMDIyMzM0M0E5QUNfdmlkZW9fZGFzaGluaXQubXA0P3N0cD1kc3QtbXA0JmVmZz1leUp4WlY5bmNtOTFjSE1pT2lKYlhDSnBaMTkzWldKZlpHVnNhWFpsY25sZmRuUnpYMjkwWmx3aVhTSXNJblpsYm1OdlpHVmZkR0ZuSWpvaWRuUnpYM1p2WkY5MWNteG5aVzR1WTJ4cGNITXVZekl1TnpJd0xtSmhjMlZzYVc1bEluMCZfbmNfY2F0PTExMCZ2cz04MTA3NTA1OTA5MTQ0ODRfMjYzMDQyMzQ1OCZfbmNfdnM9SEJrc0ZRSVlUMmxuWDNod2RsOXlaV1ZzYzE5d1pYSnRZVzVsYm5SZmNISnZaQzlGUmpReU1UYzFPVEpEUWpJd01FUXpSVFpCTURNd01qSXpNelF6UVRsQlExOTJhV1JsYjE5a1lYTm9hVzVwZEM1dGNEUVZBQUxJQVFBVkFoZzZjR0Z6YzNSb2NtOTFaMmhmWlhabGNuTjBiM0psTDBkSExYSldhSEl0YTNCeFVYZ3lhMDVCUjFOd2ExVlJSR2hYU1ZsaWNWOUZRVUZCUmhVQ0FzZ0JBQ2dBR0FBYkFCVUFBQ2FXblBHUHhlT0ZRUlVDS0FKRE15d1hRRE16TXpNek16TVlFbVJoYzJoZlltRnpaV3hwYm1WZk1WOTJNUkVBZGY0SEFBJTNEJTNEJl9uY19yaWQ9YjI2MGVjMGE4MyZjY2I9OS00Jm9oPTAwX0FZRGo1eVV5MGZHdThrMm4zbmhVQlVLMTBoS3MxcGFrZkJkM2thSG1nRHJUdkEmb2U9NjZGQUVEMjQmX25jX3NpZD0xMGQxM2I=.mp4",
            width = 750,
            height = 1333,
            username = "cris_villegas07",
            caption = "Part 1 | Jujutsu Kaisen 222-235 manga chapters Animation 🔥",
            duration = 19.2,
            userAvatarUrl = "https://scontent-yyz1-1.cdninstagram.com/v/t51.2885-19/457621679_877143554292925_4043951588544897022_n.jpg?stp=dst-jpg_e0_s150x150&_nc_ht=scontent-yyz1-1.cdninstagram.com&_nc_cat=101&_nc_ohc=BsuRG3nmJBYQ7kNvgHKLVtG&_nc_gid=b260e280f8ee4b079f61b4c67b4b2c92&edm=APs17CUBAAAA&ccb=7-5&oh=00_AYBkT_2-MZ4SwYe5vD9FCmwdMLvxhJT03wn43vDi9cDFmA&oe=66FEEF3C&_nc_sid=10d13b",
            thumbnailUrl = "https://scontent-yyz1-1.cdninstagram.com/v/t51.29350-15/441015281_460146716487560_3290678782543390784_n.jpg?stp=c0.280.720.720a_dst-jpg_e15_s640x640&_nc_ht=scontent-yyz1-1.cdninstagram.com&_nc_cat=108&_nc_ohc=6VUypcyjz54Q7kNvgHCfifs&_nc_gid=b260e280f8ee4b079f61b4c67b4b2c92&edm=APs17CUBAAAA&ccb=7-5&oh=00_AYBSTuioxdGQnAKVeFPWaHcr9efa18de73IH0RDqQqrmkg&oe=66FEDE70&_nc_sid=10d13b",
            timestamp = null,
            mediaUri = "",
        ),
        DownloadEntity(
            id = 1,
            filename = "ig-downloader-1727597487.mp4",
            contentUrl = "https://instagram.alfosuag.workers.dev/video-download/aHR0cHM6Ly9zY29udGVudC15eXoxLTEuY2RuaW5zdGFncmFtLmNvbS9vMS92L3QxNi9mMS9tODIvRUY0MjE3NTkyQ0IyMDBEM0U2QTAzMDIyMzM0M0E5QUNfdmlkZW9fZGFzaGluaXQubXA0P3N0cD1kc3QtbXA0JmVmZz1leUp4WlY5bmNtOTFjSE1pT2lKYlhDSnBaMTkzWldKZlpHVnNhWFpsY25sZmRuUnpYMjkwWmx3aVhTSXNJblpsYm1OdlpHVmZkR0ZuSWpvaWRuUnpYM1p2WkY5MWNteG5aVzR1WTJ4cGNITXVZekl1TnpJd0xtSmhjMlZzYVc1bEluMCZfbmNfY2F0PTExMCZ2cz04MTA3NTA1OTA5MTQ0ODRfMjYzMDQyMzQ1OCZfbmNfdnM9SEJrc0ZRSVlUMmxuWDNod2RsOXlaV1ZzYzE5d1pYSnRZVzVsYm5SZmNISnZaQzlGUmpReU1UYzFPVEpEUWpJd01FUXpSVFpCTURNd01qSXpNelF6UVRsQlExOTJhV1JsYjE5a1lYTm9hVzVwZEM1dGNEUVZBQUxJQVFBVkFoZzZjR0Z6YzNSb2NtOTFaMmhmWlhabGNuTjBiM0psTDBkSExYSldhSEl0YTNCeFVYZ3lhMDVCUjFOd2ExVlJSR2hYU1ZsaWNWOUZRVUZCUmhVQ0FzZ0JBQ2dBR0FBYkFCVUFBQ2FXblBHUHhlT0ZRUlVDS0FKRE15d1hRRE16TXpNek16TVlFbVJoYzJoZlltRnpaV3hwYm1WZk1WOTJNUkVBZGY0SEFBJTNEJTNEJl9uY19yaWQ9YjI2MGVjMGE4MyZjY2I9OS00Jm9oPTAwX0FZRGo1eVV5MGZHdThrMm4zbmhVQlVLMTBoS3MxcGFrZkJkM2thSG1nRHJUdkEmb2U9NjZGQUVEMjQmX25jX3NpZD0xMGQxM2I=.mp4",
            width = 750,
            height = 1333,
            username = "cris_villegas07",
            caption = "Part 1 | Jujutsu Kaisen 222-235 manga chapters Animation 🔥",
            duration = 19.2,
            userAvatarUrl = "https://scontent-yyz1-1.cdninstagram.com/v/t51.2885-19/457621679_877143554292925_4043951588544897022_n.jpg?stp=dst-jpg_e0_s150x150&_nc_ht=scontent-yyz1-1.cdninstagram.com&_nc_cat=101&_nc_ohc=BsuRG3nmJBYQ7kNvgHKLVtG&_nc_gid=b260e280f8ee4b079f61b4c67b4b2c92&edm=APs17CUBAAAA&ccb=7-5&oh=00_AYBkT_2-MZ4SwYe5vD9FCmwdMLvxhJT03wn43vDi9cDFmA&oe=66FEEF3C&_nc_sid=10d13b",
            thumbnailUrl = "https://scontent-yyz1-1.cdninstagram.com/v/t51.29350-15/441015281_460146716487560_3290678782543390784_n.jpg?stp=c0.280.720.720a_dst-jpg_e15_s640x640&_nc_ht=scontent-yyz1-1.cdninstagram.com&_nc_cat=108&_nc_ohc=6VUypcyjz54Q7kNvgHCfifs&_nc_gid=b260e280f8ee4b079f61b4c67b4b2c92&edm=APs17CUBAAAA&ccb=7-5&oh=00_AYBSTuioxdGQnAKVeFPWaHcr9efa18de73IH0RDqQqrmkg&oe=66FEDE70&_nc_sid=10d13b",
            timestamp = null,
            mediaUri = "",
        ),
        DownloadEntity(
            id = 1,
            filename = "ig-downloader-1727597487.mp4",
            contentUrl = "https://instagram.alfosuag.workers.dev/video-download/aHR0cHM6Ly9zY29udGVudC15eXoxLTEuY2RuaW5zdGFncmFtLmNvbS9vMS92L3QxNi9mMS9tODIvRUY0MjE3NTkyQ0IyMDBEM0U2QTAzMDIyMzM0M0E5QUNfdmlkZW9fZGFzaGluaXQubXA0P3N0cD1kc3QtbXA0JmVmZz1leUp4WlY5bmNtOTFjSE1pT2lKYlhDSnBaMTkzWldKZlpHVnNhWFpsY25sZmRuUnpYMjkwWmx3aVhTSXNJblpsYm1OdlpHVmZkR0ZuSWpvaWRuUnpYM1p2WkY5MWNteG5aVzR1WTJ4cGNITXVZekl1TnpJd0xtSmhjMlZzYVc1bEluMCZfbmNfY2F0PTExMCZ2cz04MTA3NTA1OTA5MTQ0ODRfMjYzMDQyMzQ1OCZfbmNfdnM9SEJrc0ZRSVlUMmxuWDNod2RsOXlaV1ZzYzE5d1pYSnRZVzVsYm5SZmNISnZaQzlGUmpReU1UYzFPVEpEUWpJd01FUXpSVFpCTURNd01qSXpNelF6UVRsQlExOTJhV1JsYjE5a1lYTm9hVzVwZEM1dGNEUVZBQUxJQVFBVkFoZzZjR0Z6YzNSb2NtOTFaMmhmWlhabGNuTjBiM0psTDBkSExYSldhSEl0YTNCeFVYZ3lhMDVCUjFOd2ExVlJSR2hYU1ZsaWNWOUZRVUZCUmhVQ0FzZ0JBQ2dBR0FBYkFCVUFBQ2FXblBHUHhlT0ZRUlVDS0FKRE15d1hRRE16TXpNek16TVlFbVJoYzJoZlltRnpaV3hwYm1WZk1WOTJNUkVBZGY0SEFBJTNEJTNEJl9uY19yaWQ9YjI2MGVjMGE4MyZjY2I9OS00Jm9oPTAwX0FZRGo1eVV5MGZHdThrMm4zbmhVQlVLMTBoS3MxcGFrZkJkM2thSG1nRHJUdkEmb2U9NjZGQUVEMjQmX25jX3NpZD0xMGQxM2I=.mp4",
            width = 750,
            height = 1333,
            username = "cris_villegas07",
            caption = "Part 1 | Jujutsu Kaisen 222-235 manga chapters Animation 🔥",
            duration = 19.2,
            userAvatarUrl = "https://scontent-yyz1-1.cdninstagram.com/v/t51.2885-19/457621679_877143554292925_4043951588544897022_n.jpg?stp=dst-jpg_e0_s150x150&_nc_ht=scontent-yyz1-1.cdninstagram.com&_nc_cat=101&_nc_ohc=BsuRG3nmJBYQ7kNvgHKLVtG&_nc_gid=b260e280f8ee4b079f61b4c67b4b2c92&edm=APs17CUBAAAA&ccb=7-5&oh=00_AYBkT_2-MZ4SwYe5vD9FCmwdMLvxhJT03wn43vDi9cDFmA&oe=66FEEF3C&_nc_sid=10d13b",
            thumbnailUrl = "https://scontent-yyz1-1.cdninstagram.com/v/t51.29350-15/441015281_460146716487560_3290678782543390784_n.jpg?stp=c0.280.720.720a_dst-jpg_e15_s640x640&_nc_ht=scontent-yyz1-1.cdninstagram.com&_nc_cat=108&_nc_ohc=6VUypcyjz54Q7kNvgHCfifs&_nc_gid=b260e280f8ee4b079f61b4c67b4b2c92&edm=APs17CUBAAAA&ccb=7-5&oh=00_AYBSTuioxdGQnAKVeFPWaHcr9efa18de73IH0RDqQqrmkg&oe=66FEDE70&_nc_sid=10d13b",
            timestamp = null,
            mediaUri = "",
        ),
    )
}
