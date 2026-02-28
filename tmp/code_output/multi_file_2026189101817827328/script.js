// DOM elements
const taskForm = document.getElementById('taskForm');
const taskTitle = document.getElementById('taskTitle');
const taskDescription = document.getElementById('taskDescription');
const taskContainer = document.getElementById('taskContainer');

// Array to store tasks
let tasks = [];

// Function to add a new task
function addTask(title, description) {
    const task = {
        id: Date.now(), // Simple unique ID based on timestamp
        title: title,
        description: description,
        completed: false
    };
    tasks.push(task);
    renderTasks();
    clearForm();
}

// Function to render all tasks
function renderTasks() {
    taskContainer.innerHTML = ''; // Clear current tasks
    tasks.forEach(task => {
        const taskElement = document.createElement('div');
        taskElement.className = 'task-item';
        taskElement.innerHTML = `
            <div>
                <h3>${task.title}</h3>
                <p>${task.description || 'No description provided.'}</p>
            </div>
            <button onclick="deleteTask(${task.id})">Delete</button>
        `;
        taskContainer.appendChild(taskElement);
    });
}

// Function to delete a task
function deleteTask(id) {
    tasks = tasks.filter(task => task.id !== id);
    renderTasks();
}

// Function to clear the form after submission
function clearForm() {
    taskTitle.value = '';
    taskDescription.value = '';
}

// Event listener for form submission
taskForm.addEventListener('submit', function(event) {
    event.preventDefault(); // Prevent page reload
    const title = taskTitle.value.trim();
    const description = taskDescription.value.trim();
    if (title) {
        addTask(title, description);
    } else {
        alert('Please enter a task title.');
    }
});

// Initialize with some example tasks (optional)
addTask('Example Task 1', 'This is an example task description.');
addTask('Example Task 2', 'Another example to demonstrate functionality.');