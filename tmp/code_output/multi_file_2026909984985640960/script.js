// 任务管理应用
document.addEventListener('DOMContentLoaded', function() {
    // 获取DOM元素
    const taskInput = document.getElementById('taskInput');
    const addTaskBtn = document.getElementById('addTaskBtn');
    const taskList = document.getElementById('taskList');
    const totalTasksEl = document.getElementById('totalTasks');
    const completedTasksEl = document.getElementById('completedTasks');
    
    // 加载保存的任务
    let tasks = JSON.parse(localStorage.getItem('tasks')) || [];
    renderTasks();
    updateStats();
    
    // 添加任务
    addTaskBtn.addEventListener('click', addTask);
    taskInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') addTask();
    });
    
    // 添加任务函数
    function addTask() {
        const taskText = taskInput.value.trim();
        if (taskText === '') return;
        
        tasks.push({
            id: Date.now(),
            text: taskText,
            completed: false
        });
        
        saveTasks();
        renderTasks();
        updateStats();
        taskInput.value = '';
        taskInput.focus();
    }
    
    // 渲染任务列表
    function renderTasks() {
        taskList.innerHTML = '';
        
        tasks.forEach(task => {
            const li = document.createElement('li');
            li.className = `task-item ${task.completed ? 'completed' : ''}`;
            li.innerHTML = `
                <span class="task-text">${task.text}</span>
                <button class="delete-btn" data-id="${task.id}">删除</button>
            `;
            
            // 点击任务文本切换完成状态
            li.querySelector('.task-text').addEventListener('click', () => {
                task.completed = !task.completed;
                saveTasks();
                renderTasks();
                updateStats();
            });
            
            // 删除任务
            li.querySelector('.delete-btn').addEventListener('click', (e) => {
                e.stopPropagation();
                tasks = tasks.filter(t => t.id !== task.id);
                saveTasks();
                renderTasks();
                updateStats();
            });
            
            taskList.appendChild(li);
        });
    }
    
    // 更新统计信息
    function updateStats() {
        totalTasksEl.textContent = tasks.length;
        const completed = tasks.filter(task => task.completed).length;
        completedTasksEl.textContent = completed;
    }
    
    // 保存任务到本地存储
    function saveTasks() {
        localStorage.setItem('tasks', JSON.stringify(tasks));
    }
});