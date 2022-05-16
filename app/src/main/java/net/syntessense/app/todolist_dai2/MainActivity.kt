package net.syntessense.app.todolist_dai2

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.syntessense.app.todolist_dai2.databinding.ActivityMainBinding
import net.syntessense.app.todolist_dai2.databinding.ListItemBinding
import org.w3c.dom.Text


class TodoAdapter(private val context: Activity, private val todoDao: TodoDao) : BaseAdapter() {

    var todos : List<Todo> = listOf()

    init {

        val self = this
        CoroutineScope(SupervisorJob()).launch {
            todoDao.deleteAll()
            for(i in 10..99)
                todoDao.insertAll(Todo(
                    0,
                    arrayOf(
                        Todo.Priority.RED,
                        Todo.Priority.ORANGE,
                        Todo.Priority.GREEN,
                    )[(0..2).random()],
                    "title $i",
                    "description $i",
                    creationDate = "20$i-01-01 00:00",
                    dueDate = "20$i-02-01 00:00",
                    doneDate = "20$i-03-01 00:00",
                ))
            todos = todoDao.getAll()
            context.runOnUiThread {
                self.notifyDataSetChanged()
            }
        }

    }

    override fun getCount(): Int {
        return todos.size
    }

    override fun getItem(position: Int): Any {
        return todos[position]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(i: Int, convertView: View?, parent: ViewGroup?): View {

        val binding = if( convertView != null) ListItemBinding.bind(convertView) else ListItemBinding.inflate(context.layoutInflater,parent,false)


        /*val cl = (convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)) as ConstraintLayout
        val tv = cl.findViewById<TextView>(R.id.item)
        val pr = cl.findViewById<TextView>(R.id.priority_color)*/

        binding.priorityColor.setBackgroundColor(todos[i].priority.color)
        binding.item.text = todos[i].title
        return binding.root
    }

}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bindings = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindings.root)
        supportActionBar?.hide()

        val speech2textLauncher = SpeechAnalysis(this)

        val dao= getTodoDb(applicationContext).todoDao()
        val fab = bindings.fab
        val lst = bindings.list
        val edt = bindings.filterBar.filterText
        val clr = bindings.filterBar.clearText
        val men = bindings.filterBar.menu
        val mic = bindings.filterBar.micro



        val adapter = TodoAdapter(this, dao)
        lst.adapter = adapter
        lst.divider = null
        clr.visibility = View.GONE

        mic.setOnClickListener {
            speech2textLauncher.start { result ->
                edt.text = Editable.Factory.getInstance().newEditable(result)
            }
        }

        edt.setOnLongClickListener {
            speech2textLauncher.start { result ->
                edt.text = Editable.Factory.getInstance().newEditable(result)
            }
            true
        }

        fab.setOnClickListener { view ->
            startActivity(Intent(this, TodoAdd::class.java))
            //adapter.add()
            //adapter.notifyDataSetChanged()
        }

        edt.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNotEmpty()) {
                    mic.visibility = View.GONE
                    clr.visibility = View.VISIBLE
                } else {
                    clr.visibility = View.GONE
                    mic.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {
            }
        })

        clr.setOnTouchListener { _, event ->
            if (event?.action == MotionEvent.ACTION_DOWN) {
                edt.text = null
            }
            clr.performClick()
            true
        }

    }

}







