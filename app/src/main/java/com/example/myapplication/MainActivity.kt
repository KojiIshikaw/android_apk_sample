package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.ScheduleEntity
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

// カテゴリーの定義：各カテゴリーに表示名と背景色を設定
enum class Category(val displayName: String, val color: Color) {
    None("未設定", Color.LightGray),
    Sleep("睡眠", Color(0xFF90CAF9)),
    Rest("休息", Color(0xFFFFF59D)),
    Work("仕事", Color(0xFFFFAB91)),
    Exercise("運動", Color(0xFFA5D6A7))
}

// 各スロットは予定内容とカテゴリーを持つ
data class ScheduleItem(val description: String, val category: Category)

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "schedule-database"
        ).build()

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MyScheduleApp(database)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyScheduleApp(database: AppDatabase) {
    val scheduleDao = database.scheduleDao()
    var scheduleSlots by remember { mutableStateOf(List(8) { ScheduleItem("", Category.None) }) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    // データベースからスケジュールを読み込む
    LaunchedEffect(Unit) {
        scheduleDao.getAllSchedules().collect { schedules ->
            val newSlots = List(8) { index ->
                schedules.find { it.timeSlot == index }?.let {
                    ScheduleItem(it.description, it.category)
                } ?: ScheduleItem("", Category.None)
            }
            scheduleSlots = newSlots
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("スケジュール管理アプリ") })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            item { TimeDivider(time = "0時") }
            
            for (index in scheduleSlots.indices) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { editingIndex = index },
                        colors = CardDefaults.cardColors(containerColor = scheduleSlots[index].category.color),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (scheduleSlots[index].description.isNotBlank())
                                    scheduleSlots[index].description
                                else "未設定",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = scheduleSlots[index].category.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
                when (index) {
                    1 -> item { TimeDivider(time = "6時") }
                    3 -> item { TimeDivider(time = "12時") }
                    5 -> item { TimeDivider(time = "18時") }
                    7 -> item { TimeDivider(time = "24時") }
                }
            }
        }
        
        if (editingIndex != null) {
            val currentSlot = scheduleSlots[editingIndex!!]
            var descriptionInput by remember { mutableStateOf(currentSlot.description) }
            var selectedCategory by remember { mutableStateOf(currentSlot.category) }
            var expanded by remember { mutableStateOf(false) }
            
            AlertDialog(
                onDismissRequest = { editingIndex = null },
                title = { Text("スケジュール編集") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = descriptionInput,
                            onValueChange = { descriptionInput = it },
                            label = { Text("予定内容") },
                            placeholder = { Text("例: ミーティング") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Box {
                            OutlinedButton(
                                onClick = { expanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(selectedCategory.displayName)
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Category.values().forEach { category ->
                                    DropdownMenuItem(
                                        text = { 
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .background(category.color)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(category.displayName)
                                            }
                                        },
                                        onClick = {
                                            selectedCategory = category
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                val newSchedule = ScheduleEntity(
                                    timeSlot = editingIndex!!,
                                    description = descriptionInput,
                                    category = selectedCategory
                                )
                                scheduleDao.insertSchedule(newSchedule)
                                
                                scheduleSlots = scheduleSlots.toMutableList().also {
                                    it[editingIndex!!] = it[editingIndex!!].copy(
                                        description = descriptionInput,
                                        category = selectedCategory
                                    )
                                }
                                editingIndex = null
                            }
                        }
                    ) {
                        Text("保存")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingIndex = null }) {
                        Text("キャンセル")
                    }
                }
            )
        }
    }
}

@Composable
fun TimeDivider(time: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Divider(modifier = Modifier.weight(1f))
        Text(
            text = time,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Divider(modifier = Modifier.weight(1f))
    }
}
