package com.app.maskit_app


import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import utils.BitmapUtils

/**
 * list to hold all the filters (for coloring only the selected filter
 */
private val filterList: ArrayList<FilterHolder> = ArrayList()

fun createFilters(): List<Filter> {
    val list = mutableListOf<Filter>()
    list += Filter("Normal.png", " Normal", Filter.NORMAL)
    list += Filter("strongBlur1.png", " Blur", Filter.BLUR)
    list += Filter("smiley.png", " Smile", Filter.SMILE)
    list += Filter("sad.png", " Sad", Filter.SAD)
    list += Filter("covidMask.png", " Face Mask", Filter.COVID)
    list += Filter("blackCube.png", " Black Cube", Filter.BlackCube)
    list += Filter("emojiFaceWithCoronaMask.png", " Emoji Face With Mask", Filter.FaceWithCovidMask)
    // Marvel Filters
    list += Filter("Marvel_Icons/Marvel_Captain_America.png", " Marvel Captain America",
            Filter.MarvelCaptainAmerica)
    list += Filter("Marvel_Icons/Marvel_Iron_Man.png", " Marvel Iron Man", Filter.MarvelIronMan)
    list += Filter("Marvel_Icons/Marvel_Thanos.png", " Marvel Thanos", Filter.MarvelThanos)
    list += Filter("Marvel_Icons/Marvel_Groot.png", " Marvel Groot", Filter.MarvelGroot)
    list += Filter("Marvel_Icons/Marvel_Thor.png", " Marvel Thor", Filter.MarvelThor)
    list += Filter("Marvel_Icons/Marvel_Wolverine.png", " Marvel Wolverine", Filter.MarvelWolverine)
    list += Filter("Marvel_Icons/Marvel_Hulk.png", " Marvel Hulk", Filter.MarvelHulk)
    list += Filter("Marvel_Icons/Marvel_Deadpool.png", " Marvel Deadpool", Filter.MarvelDeadpool)
    list += Filter("Marvel_Icons/Marvel_Beast.png", " Marvel Beast", Filter.MarvelBeast)
    list += Filter("Marvel_Icons/Marvel_Cyclops.png", " Marvel Cyclop", Filter.MarvelCyclops)
    // Animals
    list += Filter("Animals_Icon/Angry_Dog.png", " Angry Dog", Filter.AngryDog)
    list += Filter("Animals_Icon/Chicken.png", " Chicken", Filter.Chicken)
    list += Filter("Animals_Icon/Cow.png", " Cow", Filter.Cow)
    list += Filter("Animals_Icon/Fox.png", " Fox", Filter.Fox)
    list += Filter("Animals_Icon/Owl.png", " Owl", Filter.Owl)
    list += Filter("Animals_Icon/Panda.png", " Panda", Filter.Panda)
    list += Filter("Animals_Icon/Pixelated_Cat.png", " Pixelated Cat", Filter.PixelatedCat)
    list += Filter("Animals_Icon/Pug_Dog.png", " Pug Dog", Filter.PugDog)
    list += Filter("Animals_Icon/Racoon.png", " Racoon", Filter.Racoon)
    list += Filter("Animals_Icon/Red_Panda.png", " RedPanda", Filter.RedPanda)
    list += Filter("Animals_Icon/Reindeer.png", " Reindeer", Filter.Reindeer)
    list += Filter("Animals_Icon/Sloth.png", " Sloth", Filter.Sloth)
    list += Filter("Animals_Icon/Whale.png", " Whale", Filter.Whale)
    return list
}

/**
 * hold each filter in the recycler
 */
class FilterHolder(view: View): RecyclerView.ViewHolder(view) ,  View.OnClickListener {
    val text: TextView = view.findViewById(R.id.filter_text)
    val img: ImageView = view.findViewById(R.id.filter_image)

    override fun onClick(itemView: View?) {
        itemView!!.setOnClickListener(this)
    }
}

/**
 * adapter for the recycler
 */
class FilterAdapter: RecyclerView.Adapter<FilterHolder>(), Filterable{
    var onTaskClickCallback: OnFilterClickListener? = null
    private val _filters: MutableList<Filter> = ArrayList()
    private val allFilters: MutableList<Filter> = ArrayList()
    var context:Context?=null
    var currentFilter : Int?=null
    
    override fun getItemCount(): Int {
        return _filters.size
    }

    fun setFilters() {
        _filters.clear()
        _filters.addAll(createFilters())
        allFilters.addAll(createFilters())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterHolder {
        context = parent.context
        val view = LayoutInflater.from(context)
                .inflate(R.layout.item_one_filter, parent, false)
        val taskHolder = FilterHolder(view)
        filterList.add(taskHolder)
        return taskHolder

    }

    fun getAllFiltersArr(): MutableList<Filter> {
        return allFilters
    }
    fun clearFilters(){
        _filters.clear()
    }
    fun setFilter(arr : ArrayList<Filter>){
        _filters.addAll(arr)
    }

    override fun onBindViewHolder(holder: FilterHolder, position: Int) {
        // get the person
        val filter = _filters[position]

        holder.text.text = filter.description
        holder.img.setImageBitmap(BitmapUtils.getBitmapFromAssets(context, filter.path, 300, 300))
        if (filter.filter == currentFilter){
            holder.itemView.setBackgroundColor(Color.LTGRAY)
        }
        else{
            holder.itemView.setBackgroundColor(Color.WHITE)
        }
        holder.itemView.setOnClickListener {
            val listener = onTaskClickCallback
            currentFilter = filter.filter

            if (listener != null) {
                listener.onTaskClicked(filter)
                for ( t in filterList){
                    t.itemView.setBackgroundColor(Color.WHITE)
                }
                holder.itemView.setBackgroundColor(Color.LTGRAY)
                // animation upon click
                val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.moving_down_and_up)
                holder.itemView.startAnimation(animation)
            }
        }

    }

    override fun getFilter(): android.widget.Filter {
        TODO("Not yet implemented")
    }

}

