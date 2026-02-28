// 任务管理应用
document.addEventListener('DOMContentLoaded', function() {
    // DOM元素
    const taskInput = document.getElementById('taskInput');
    const addTaskBtn = document.getElementById('addTaskBtn');
    const taskList = document.getElementById('taskList');
    const totalTasksEl = document.getElementById('totalTasks');
    const completedTasksEl = document.getElementById('completedTasks');
    const pendingTasksEl = document.getElementById('pendingTasks');
    
    // 任务数组
    let tasks = [];
    
    // 从本地存储加载任务
    loadTasksFromStorage();
    
    // 添加任务事件监听
    addTaskBtn.addEventListener('click', addTask);
    taskInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            addTask();
        }
    });
    
    // 添加新任务
    function addTask() {
        const taskText = taskInput.value.trim();
        
        if (taskText === '') {
            alert('请输入任务内容');
            return;
        }
        
        // 创建新任务对象
        const newTask = {
            id: Date.now(),
            text: taskText,
            completed: false,
            createdAt: new Date().toISOString()
        };
        
        // 添加到任务数组
        tasks.push(newTask);
        
        // 清空输入框
        taskInput.value = '';
        
        // 保存到本地存储
        saveTasksToStorage();
        
        // 更新UI
        renderTasks();
        
        // 聚焦到输入框
        taskInput.focus();
    }
    
    // 切换任务完成状态
    function toggleTaskCompletion(taskId) {
        const taskIndex = tasks.findIndex(task => task.id === taskId);
        
        if (taskIndex !== -1) {
            tasks[taskIndex].completed = !tasks[taskIndex].completed;
            saveTasksToStorage();
            renderTasks();
        }
    }
    
    // 删除任务
    function deleteTask(taskId) {
        if (confirm('确定要删除这个任务吗？')) {
            tasks = tasks.filter(task => task.id !== taskId);
            saveTasksToStorage();
            renderTasks();
        }
    }
    
    // 渲染任务列表
    function renderTasks() {
        // 更新统计信息
        updateStats();
        
        // 清空任务列表
        taskList.innerHTML = '';
        
        // 如果没有任务，显示空状态
        if (tasks.length === 0) {
            const emptyState = document.createElement('div');
            emptyState.className = 'empty-state';
            emptyState.innerHTML = `
                <i class="fas fa-clipboard-list"></i>
                <p>暂无任务，添加您的第一个任务吧！</p>
            `;
            taskList.appendChild(emptyState);
            return;
        }
        
        // 渲染每个任务
        tasks.forEach(task => {
            const taskItem = document.createElement('div');
            taskItem.className = `task-item ${task.completed ? 'completed' : ''}`;
            taskItem.innerHTML = `
                <input type="checkbox" class="task-checkbox" ${task.completed ? 'checked' : ''}>
                <div class="task-text">${task.text}</div>
                <div class="task-actions">
                    <button class="task-btn delete-btn" title="删除任务">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            `;
            
            // 添加事件监听
            const checkbox = taskItem.querySelector('.task-checkbox');
            const deleteBtn = taskItem.querySelector('.delete-btn');
            
            checkbox.addEventListener('change', () => toggleTaskCompletion(task.id));
            deleteBtn.addEventListener('click', () => deleteTask(task.id));
            
            taskList.appendChild(taskItem);
        });
    }
    
    // 更新统计信息
    function updateStats() {
        const total = tasks.length;
        const completed = tasks.filter(task => task.completed).length;
        const pending = total - completed;
        
        totalTasksEl.textContent = `总任务: ${total}`;
        completedTasksEl.textContent = `已完成: ${completed}`;
        pendingTasksEl.textContent = `待完成: ${pending}`;
    }
    
    // 保存任务到本地存储
    function saveTasksToStorage() {
        localStorage.setItem('taskRecords', JSON.stringify(tasks));
    }
    
    // 从本地存储加载任务
    function loadTasksFromStorage() {
        const storedTasks = localStorage.getItem('taskRecords');
        
        if (storedTasks) {
            try {
                tasks = JSON.parse(storedTasks);
                renderTasks();
            } catch (e) {
                console.error('加载任务失败:', e);
                tasks = [];
            }
        }
    }
    
    // 初始渲染
    renderTasks();
});