import random
import tkinter as tk
import time
from tkinter import messagebox

body = []  # Snake body position
food1 = []   # Food1 positions (red; increase one unit)
food2 = []   # Food2 positions (blue; increase two units)
direction = None  # Snake direction (0: right, 1: up, 2: left, 3: down)
points = None   # Current points
in_game = None  # Whether the game is still on
game_over_info = None   # Whether the game over message has been displayed
start_time = None  # Start time of the game


# Open the game window
def open_game_window():
    global window, points, body, food1, direction, in_game, game_over_info, start_time

    # Clear the menu window. Clear the previous food1 and food2 positions.
    clear_window(window)
    food1.clear()
    food2.clear()

    # Title label
    title_label = tk.Label(window, text="Snake Game", font=("Gloucester MT Extra Condensed", 50, "bold"), fg="green")
    title_label.pack()

    # Top frame for points label and exit button
    top_frame = tk.Frame(window)
    top_frame.pack(fill=tk.X, padx=20, pady=20)

    # Points label
    points_label = tk.Label(top_frame, text=f"Points: {points}", font=("Lucida Bright", 28, "bold"))
    points_label.pack(side=tk.LEFT)
    
    # Exit to menu button
    exit_button = tk.Button(top_frame, text="Exit to Menu", font=("Lucida Sans", 24), command=lambda: return_menu(window))
    exit_button.pack(side=tk.RIGHT)

    # Snake game canvas
    canvas = tk.Canvas(window, width=600, height=600, bg="beige")
    canvas.pack()
    
    # Draw boundary
    canvas.create_rectangle(5, 5, 595, 595, outline="black", width=5) 
    
    # Initialize default snake postion
    body = [(5, 5), (6, 5)]

    # Generate initial food1 positions
    for _ in range(5):  
        new_food1()

    # Generate initial food2 positions
    new_food2()
    
    # Default position: right
    direction = 0  

    # Initial game status.
    points = 0
    in_game = True  
    game_over_info = False 

    # Record start time
    start_time = time.time()  

    # Bind keyboard movement
    window.bind("<KeyPress>", read_keyboard)  

    # Draw initial snake and food
    draw_snake_food(canvas)

    # Update game periodically
    def update():
        global in_game, points, game_over_info

        # Move snake, then draw
        update_snake(canvas, points_label)
        update_canvas(canvas, points_label)

        # Check winning status
        if points >= 6:
            duration = time.time() - start_time
            messagebox.showinfo("Game over", f"You Win! The snake reaches {points + 2} units!\nTime taken: {duration:.2f} seconds.")

            # Game over, no more updates.
            in_game = False

            # Set game_over_info to True to prevent multiple popups
            game_over_info = True  

        # Schedule the next update after 100ms if not game over
        if in_game:
            window.after(100, update)
    
    # Start updating
    if in_game:
        update()

    
# Return to menu
def return_menu(window):
    global in_game

    # Terminate the game
    in_game = False  

    # Clear the game window and show the menu
    clear_window(window)
    show_menu()

# Read the keyboard and get the direction
def read_keyboard(event):
    global direction
    if event.keysym == "d" or event.keysym == "Right": 
        direction = 0
    elif event.keysym == "w" or event.keysym == "Up":
        direction = 1
    elif event.keysym == "a" or event.keysym == "Left":
        direction = 2
    elif event.keysym == "s" or event.keysym == "Down":
        direction = 3

# Draw the current snake and foods
def draw_snake_food(canvas):
    # Draw snake
    for segment in body:
        x, y = segment
        canvas.create_rectangle(x * 20, y * 20, x * 20 + 20, y * 20 + 20, fill="green")

    # Draw foods
    for item in food1:
        x, y = item
        canvas.create_rectangle(x * 20, y * 20, x * 20 + 20, y * 20 + 20, fill="red")
    for item in food2:
        x, y = item
        canvas.create_rectangle(x * 20, y * 20, x * 20 + 20, y * 20 + 20, fill="blue")


# Update the snake's position based on direction
def update_snake(canvas, points_label):
    global body, direction, food1, points, game_over_info, in_game, start_time

    # Determine new head position
    head_x, head_y = body[-1]
    if direction == 0:
        new_head = (head_x + 1, head_y)
    elif direction == 1:
        new_head = (head_x, head_y - 1)
    elif direction == 2:
        new_head = (head_x - 1, head_y)
    elif direction == 3:
        new_head = (head_x, head_y + 1)

    # Check eating food1
    if new_head in food1:
        food1.remove(new_head)
        new_food1()  
        points += 1
        body.append(new_head)

    # Check eating food2, increasing length by 2 units
    elif new_head in food2:
        food2.remove(new_head)
        new_food2()
        points += 2  
        body.append(new_head)
        tail_grow()

    # Check whether the snake collides with its own body or the boundary
    elif new_head in body or new_head[0] <= 0 or new_head[0] >= 29 or new_head[1] <= 0 or new_head[1] >= 29:
        if not game_over_info:
            body.pop(0)
            body.append(new_head)
            update_canvas(canvas, points_label)
            duration = time.time() - start_time
            messagebox.showinfo("Game Over", f"Game Over! Snake length:  {points + 2} units.\nTime taken: {duration:.2f} seconds.")
            game_over_info = True  
        in_game = False

    # Move by one unit, with no increasement in length
    else:
        body.pop(0)
        body.append(new_head) 

# Generate new food1
def new_food1():
    global body, food1, food2
    next = (random.randint(1, 28), random.randint(1, 28))
    if next in body or next in food1 or next in food2:
        new_food1()
    else:
        food1.append(next)

# Generate new food2
def new_food2():
    global body, food1, food2
    next = (random.randint(1, 28), random.randint(1, 28))
    if next in body or next in food1 or next in food2:
        new_food2()
    else:
        food2.append(next)

# Helper method to for the snake to grow by a unit at its tail
def tail_grow():
    global body
    x = body[1][0] - body[0][0]
    y = body[1][1] - body[0][1]
    if x == 0:
        if y == 1:
            body = [(body[0][0], body[0][1] - 1)] + body
        else:
            body = [(body[0][0], body[0][1] + 1)] + body
    else:
        if x == 1:
            body = [(body[0][0] - 1, body[0][1])] + body
        else:
            body = [(body[0][0] + 1, body[0][1])] + body


# Draw the new canvas and update point label
def update_canvas(canvas, points_label):
    if canvas.winfo_exists():
        # Clear the canvas
        canvas.delete("all") 

        # Redraw snake, food and boundary
        draw_snake_food(canvas)  
        canvas.create_rectangle(5, 5, 595, 595, outline="black", width=5) 

        # Update points label
        points_label.config(text=f"Points: {points}")  


# Exit the game
def exit_game():
    window.destroy()


# Clear the current window
def clear_window(window):
    for widget in window.winfo_children():
        widget.destroy()


# Show the menu window
def show_menu():
    global window

    # Clear the window
    clear_window(window)

    # Title label
    title_label = tk.Label(window, text="Snake Game", font=("Gloucester MT Extra Condensed", 50, "bold"), fg="green")
    title_label.pack(pady=200)

    # Horizontal frame for start button and exit button
    se_box = tk.Frame(window)
    se_box.pack(pady=20)

    # Start button
    start_button = tk.Button(se_box, text="Start", font=("Lucida Sans", 36, "bold"), command=open_game_window)
    start_button.pack(side=tk.LEFT, padx=20)

    # Exit button
    exit_button = tk.Button(se_box, text="Exit", font=("Lucida Sans", 36, "bold"), command=exit_game)
    exit_button.pack(side=tk.LEFT, padx=20)


# Create the menu window
window = tk.Tk()
window.title("Snake Game")

# Make sure the window is generated at the center of the screen
win_x = window.winfo_screenwidth() // 2 - 400
win_y = window.winfo_screenheight() // 2 - 400
window.geometry(f"800x800+{win_x}+{win_y}")

# Initial screen: menu
show_menu()  
window.mainloop()