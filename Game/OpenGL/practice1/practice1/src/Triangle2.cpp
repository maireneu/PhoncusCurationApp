#include <iostream>
#include <sstream>
#include <memory>
#include <string>

#define GLEW_STATIC
#include "GL/glew.h"
#include "GLFW/glfw3.h"
#include "glm/glm.hpp"

#include "ShaderProgram.h"
#include "Texture2D.h"

const char* APP_TITLE = "Hello, Modern OpenGL";
int gWindowWidth = 1024;
int gWindowHeight = 768;
bool gWireframe = false;
//GLFWwindow* gWindow = nullptr;
struct DestroyglfwWin {
	void operator()(GLFWwindow* ptr) {
		glfwDestroyWindow(ptr);
	}
};

auto gWindow = std::unique_ptr<GLFWwindow, DestroyglfwWin>();

bool gFullscreen = false;
const std::string texture1Filename = "textures/airplane.PNG";
const std::string texture2Filename = "textures/crate.jpg";

void glfw_onKey(GLFWwindow* window, int key, int scancode, int action, int mode);
void glfw_OnFrameBufferSize(GLFWwindow* window, int width, int height);
void showFPS(GLFWwindow* window);
bool initOpenGL();

int main()
{

	if (!initOpenGL()) {
		std::cerr << "GLFW initialization failed" << std::endl;
		return -1;
	}

	GLfloat vert_pos[] = {
		// position			// tex coords
		-0.5f,  0.5f,  0.0f, 0.0f, 1.0f, // top left
		 0.5f,  0.5f,  0.0f, 1.0f, 1.0f, // top right
		 0.5f, -0.5f,  0.0f, 1.0f, 0.0f, // bottom right
		-0.5f, -0.5f,  0.0f, 0.0f, 0.0f  // bottom left
	};

	GLuint indices[] = {
		0, 1, 2,	// tri 0
		0, 2, 3		// tir 1
	};

	GLuint vbo, ibo, vao;

	//gen vertexarray
	glGenVertexArrays(1, &vao);
	glBindVertexArray(vao);

	//gen position buffer
	glGenBuffers(1, &vbo);
	glBindBuffer(GL_ARRAY_BUFFER, vbo);
	glBufferData(GL_ARRAY_BUFFER, sizeof(vert_pos), vert_pos, GL_STATIC_DRAW);
	
	glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 5 * sizeof(GLfloat), (GLvoid*)(0));
	glEnableVertexAttribArray(0);

	 
	glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 5 * sizeof(GLfloat), (GLvoid*)(3* sizeof(GLfloat)));
	glEnableVertexAttribArray(1);

	glGenBuffers(1, &ibo);
	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
	glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(indices), indices, GL_STATIC_DRAW);

	ShaderProgram shaderProgram;
	shaderProgram.loadShaders("basic.vert", "basic.frag");

	Texture2D texture1;
	texture1.loadTexture(texture1Filename, true);

	Texture2D texture2;
	texture2.loadTexture(texture2Filename, 2);

	// Main loop
	while (!glfwWindowShouldClose(gWindow.get())) {
		showFPS(gWindow.get());
		glfwPollEvents();
		glClear(GL_COLOR_BUFFER_BIT);

		texture1.bind(0);
		texture2.bind(1);

		shaderProgram.use();

		glUniform1i(glGetUniformLocation(shaderProgram.GetProgram(), "myTexture1"), 0);
		glUniform1i(glGetUniformLocation(shaderProgram.GetProgram(), "myTexture2"), 1);


		//GLfloat time = glfwGetTime();
		//GLfloat Color = (sin(time) / 2) + 0.5;
		//glm::vec2 pos;
		//pos.x = sin(time) / 2;
		//pos.y = cos(time) / 2;
		//shaderProgram.setUniform("posOffset", pos);
		//shaderProgram.setUniform("vertColor", glm::vec4(Color, Color, Color, 1.0f));


		glBindVertexArray(vao);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
		glBindVertexArray(0);

		glfwSwapBuffers(gWindow.get());
	}

	glDeleteVertexArrays(1, &vao);
	glDeleteBuffers(1, &vbo);
	glDeleteBuffers(1, &ibo);

	glfwTerminate();

	return 0;
}

bool initOpenGL() {

	if (!glfwInit()) {
		std::cerr << "GLFW initialization failed" << std::endl;
		return false;
	}
	glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
	glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
	glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
	glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

	//auto gWindow = std::unique_ptr<GLFWwindow, DestroyglfwWin>();
	//auto pWindow = std::make_unique<GLFWwindow>(glfwCreateWindow(gWindowWidth, gWindowHeight, APP_TITLE, NULL, NULL));

	if (gFullscreen) {
		GLFWmonitor* pMonitor = glfwGetPrimaryMonitor();
		const GLFWvidmode* pVmode = glfwGetVideoMode(pMonitor);
		if (pVmode != NULL) {
			gWindow.reset(glfwCreateWindow(pVmode->width, pVmode->height, APP_TITLE, pMonitor, NULL));
		}
	}
	else {
		gWindow.reset(glfwCreateWindow(gWindowWidth, gWindowHeight, APP_TITLE, NULL, NULL));
	}

	if (gWindow == nullptr) {
		std::cerr << "Failed to create GLFW window" << std::endl;
		return false;
	}

	glfwMakeContextCurrent(gWindow.get());
	glfwSwapInterval(1);
	glfwSetKeyCallback(gWindow.get(), glfw_onKey);

	glewExperimental = GL_TRUE;
	if (glewInit() != GLEW_OK) {
		std::cerr << "GLFW initialization failed" << std::endl;
		return false;
	}

	glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
	glViewport(0, 0, gWindowWidth, gWindowHeight);
	return true;

}

void glfw_onKey(GLFWwindow* window, int key, int scancode, int action, int mode) {
	if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS)
		glfwSetWindowShouldClose(window, GL_TRUE);

	if (key == GLFW_KEY_W && action == GLFW_PRESS) {
		gWireframe = !gWireframe;
		if (gWireframe)	
			glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		else
			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
	}

}

void glfw_OnFrameBufferSize(GLFWwindow* window, int width, int height) {
	gWindowWidth = width;
	gWindowHeight = height;
	glViewport(0, 0, gWindowWidth, gWindowHeight);
}

void showFPS(GLFWwindow* window) {
	static double previousSeconds = 0.0;
	static int frameCount = 0;
	double elapsedSeconds;
	// returns number of seconds since GLFW started, as a double
	double currentSeconds = glfwGetTime();

	elapsedSeconds = currentSeconds - previousSeconds;
	//limit text update 4 times per second
	if (elapsedSeconds > 0.25) {
		previousSeconds = currentSeconds;
		double fps = (double)frameCount / elapsedSeconds;
		double msPerFrame = 1000.0 / fps;

		std::ostringstream outs;
		outs.precision(3);
		outs << std::fixed
			<< APP_TITLE << "   "
			<< "FPS: " << fps << "   "
			<< "Frame Time: " << msPerFrame << "  (ms)";
		glfwSetWindowTitle(window, outs.str().c_str());

		frameCount = 0;
	}

	frameCount++;
}