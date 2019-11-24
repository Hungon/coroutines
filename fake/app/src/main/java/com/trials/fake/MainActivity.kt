package com.trials.fake

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.trials.fake.main.MainNetworkImpl
import com.trials.fake.main.MainViewModel
import com.trials.fake.main.TitleRepository
import com.trials.fake.main.getDatabase
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity(), CoroutineScope {

    private val target = 0
    private val superVisorJob = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + superVisorJob

    /**
     * Inflate layout.activity_main and setup data binding.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val rootLayout: ConstraintLayout = findViewById(R.id.rootLayout)
        val title: TextView = findViewById(R.id.title)
        val spinner: ProgressBar = findViewById(R.id.spinner)

        // Get MainViewModel by passing a database to the factory
        val database = getDatabase(this)
        val repository = TitleRepository(MainNetworkImpl, database.titleDao)
        val viewModel = ViewModelProviders
            .of(this, MainViewModel.FACTORY(repository))
            .get(MainViewModel::class.java)

        // When rootLayout is clicked call onMainViewClicked in ViewModel
        rootLayout.setOnClickListener {
            viewModel.onMainViewClicked()
        }

        // update the title when the [MainViewModel.title] changes
        viewModel.title.observe(this, Observer { value ->
            value?.let {
                title.text = it
            }
        })

        // show the spinner when [MainViewModel.spinner] is true
        viewModel.spinner.observe(this, Observer { value ->
            value?.let { show ->
                spinner.visibility = if (show) View.VISIBLE else View.GONE
            }
        })

        // Show a snackbar whenever the [ViewModel.snackbar] is updated a non-null value
        viewModel.snackbar.observe(this, Observer { text ->
            text?.let {
                Snackbar.make(rootLayout, text, Snackbar.LENGTH_SHORT).show()
                viewModel.onSnackbarShown()
            }
        })
    }


    override fun onResume() {
        super.onResume()
        var total1 = 0L
        var total2 = 0L
        val max = 100
        val size = 300
        launch {
            repeat(max) {
                val randomNum = if (it % 2 == 0) Random.nextInt(0, size) else size
                val list = List(size) { index ->
                    if (index == randomNum) {
                        0
                    } else {
                        1
                    }
                }
                Log.e(
                    MainActivity::class.java.simpleName,
                    "($it) list size -> ${list.size} random num -> $randomNum"
                )
                val time = measureTimeMillis {
                    val res = list.process01(CoroutineScope(coroutineContext))
                    Log.d(
                        MainActivity::class.java.simpleName, "func1(): res -> $res"
                    )
                }
                total1 += time
                Log.d(
                    MainActivity::class.java.simpleName,
                    "func1(): processing time -> $time, total -> $total1 ave -> ${total1 / (it + 1)}"
                )
                val time3 = measureTimeMillis {
                    val res = func2(list)
                    Log.d(
                        MainActivity::class.java.simpleName,
                        "func3(): res -> $res"
                    )
                }
                total2 += time3
                Log.d(
                    MainActivity::class.java.simpleName,
                    "func3(): processing time -> $time3, total -> $total2 ave -> ${total2 / (it + 1)}"
                )
                delay(100L)
            }
        }
    }

    private suspend fun func2(list: List<Int>): Boolean {
        var res = true
        for (i in list) {
            if (!check(i, target)) {
                res = false
                break
            }
        }
        return res
    }

    private suspend fun check(index: Int, target: Int): Boolean {
        delay(15L)
        return (index != target)
    }

    private suspend inline fun List<Int>.process01(scope: CoroutineScope) =
        suspendCoroutine<Boolean> { continuation ->
            scope.launch {
                withContext(Dispatchers.Default) {
                    for (i in iterator()) {
                        CoroutineScope(this@withContext.coroutineContext).launch {
                            if (!check(i, target)) {
                                scope.coroutineContext.cancelChildren()
                                continuation.resume(false)
                            }
                        }
                    }
                }
                continuation.resume(true)
            }
        }

    private suspend inline fun List<Int>.process02(parent: CoroutineScope): Boolean =
        coroutineScope {
            val separator = 2
            val size = size
            val list1 = subList(0, size / separator)
            val list2 = subList(size / separator, size)
            val res1 = parent.async { func2(list1) }
            val res2 = parent.async { func2(list2) }
            val res = res1.await() && res2.await()
            return@coroutineScope res
        }

}
