package com.example.homework14

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.bumptech.glide.Glide
import com.example.homework14.JSON.FilmsList
import com.example.homework14.Retrofit2.RetrofitServieces
import com.example.homework14.Retrofit2.RetrofitClient
import com.example.homework14.db.AppBase
import com.example.homework14.db.FilmDataO
import com.example.homework14.db.Films
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    lateinit var filmData: FilmDataO
    private var newList: ArrayList<Films> = arrayListOf<Films>()
    private var yearList: ArrayList<Films> = ArrayList<Films>()
    private var posterList: ArrayList<String> = arrayListOf<String>()
    lateinit var films: ArrayList<Films>
    var tempFilms: ArrayList<Films> = arrayListOf<Films>()

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = Room.databaseBuilder(
            applicationContext,
            AppBase::class.java, "my-app-database-films"
        ).build()
        filmData = db.filmDataO()

        val recyclerView = findViewById<RecyclerView>(R.id.recycleViewID)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, 1))
        val searchButton = findViewById<Button>(R.id.buttonFindID)
        val editTextYear = findViewById<EditText>(R.id.editTextID)
        val saveButton = findViewById<Button>(R.id.buttonSaveDb)
        val clearButton = findViewById<Button>(R.id.btClearDB)
        val request = RetrofitClient.buildService(RetrofitServieces::class.java)

        searchButton.setOnClickListener {
            if (!(editTextYear.text.isNullOrEmpty())) {
                val call = request.getMovies(
                    "popularity.desc", "8123971648da4ff0ca36b9fed8f72ebd",
                    editTextYear.text.toString(), "ru"
                )

                call.enqueue(object : Callback<FilmsList> {
                    override fun onResponse(call: Call<FilmsList>, response: Response<FilmsList>) {
                        if (response.isSuccessful) {
                            newList.clear()

                            (0..(response.body()?.movies?.lastIndex!!))
                                .forEach { n ->
                                    newList.add(
                                        Films(
                                            response.body()?.movies?.get(n)?.title.toString(),
                                            response.body()?.movies?.get(n)?.voteAverage.toString(),
                                            response.body()?.movies?.get(n)?.overview.toString(),
                                            response.body()?.movies?.get(n)?.releaseDate.toString()
                                                .substringBefore("-"),
                                            "https://image.tmdb.org/t/p/w500" + response?.body()?.movies?.get(
                                                n
                                            )?.posterPath.toString()
                                        )
                                    )
                                }
                            recyclerView.adapter = RecyclerAdapter(newList)
                        }
                    }

                    override fun onFailure(call: Call<FilmsList>, t: Throwable) {

                        Toast.makeText(
                            this@MainActivity,
                            "No internet, trying to load from Base",
                            Toast.LENGTH_LONG
                        ).show()
                        lifecycleScope.launch(Dispatchers.IO) {
                            yearList.clear()
                            val yearResult = editTextYear.text.toString().toInt()
                            yearList = filmData.loadAllByYear(yearResult) as ArrayList<Films>
                            withContext(Dispatchers.Main)
                            {
                                recyclerView.adapter = RecyclerAdapter(yearList as ArrayList<Films>)
                            }
                        }
                    }
                })
            }
            if (editTextYear.text.isNullOrEmpty()) {
                Toast.makeText(this@MainActivity, "Please input year", Toast.LENGTH_LONG).show()
            }
        }

        saveButton.setOnClickListener {

            Toast.makeText(this@MainActivity, "Film list are saved to base", Toast.LENGTH_LONG)
                .show()
            lifecycleScope.launch(Dispatchers.IO) {
                for (k in 0..newList.lastIndex) {
                    filmData.insertAll(newList[k])
                }
            }
        }

        clearButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                filmData.removeAll()
            }
            Toast.makeText(this@MainActivity, "Base was cleared", Toast.LENGTH_LONG)
                .show()
        }
    }
}

public class RecyclerAdapter(private val items: ArrayList<Films>) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val filmTitleView: TextView = view.findViewById(R.id.textTitle)
        val filmVoteAverageView: TextView = view.findViewById(R.id.textVoteAverage)
        val filmOverviewView: TextView = view.findViewById(R.id.textOverView)
        val filmPosterView: ImageView = view.findViewById(R.id.posterItem)
    }

    override fun getItemCount() = items.size
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recycle_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        items[position].apply {
            viewHolder.filmTitleView.text = filmTitle
            viewHolder.filmVoteAverageView.text = filmVoteAverage
            viewHolder.filmOverviewView.text = filmOverview
            Glide.with(viewHolder.filmPosterView.context)
                .load(items[position].filmPoster)
                .override(120, 120)
                .into(viewHolder.filmPosterView)
        }
    }
}
