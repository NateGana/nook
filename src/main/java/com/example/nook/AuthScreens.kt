package com.example.nook

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nook.ui.theme.*

@Composable
fun NookField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false
) {
    TextField(
        value,
        onValueChange,
        placeholder = { Text(placeholder, color = NookMuted, fontSize = 13.sp) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = NookInput,
            unfocusedContainerColor = NookInput,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
fun OrDivider() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(Modifier.weight(1f), color = NookInput)
        Text("  or  ", color = NookMuted, fontSize = 14.sp)
        HorizontalDivider(Modifier.weight(1f), color = NookInput)
    }
}

@Composable
fun SocialRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        Image(painterResource(R.drawable.ic_facebook), "Continue with Facebook", Modifier.size(28.dp))
        Image(painterResource(R.drawable.ic_google), "Continue with Google", Modifier.size(28.dp))
    }
}

@Composable
fun GetStartedScreen(onNext: () -> Unit) {
    Column(Modifier.fillMaxSize().background(NookCream).statusBarsPadding().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        NookLogo(150)
        Spacer(Modifier.height(24.dp))
        Text("nook", fontSize = 44.sp, fontWeight = FontWeight.ExtraBold, color = NookText)
        Spacer(Modifier.height(12.dp))
        Text("Put it down,\nwe'll find it.", color = NookMuted, fontSize = 15.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(48.dp))
        NookFilledButton("Get Started", onNext, Modifier.width(220.dp))
    }
}

@Composable
fun LoginLandingScreen(onLogin: () -> Unit, onSignUp: () -> Unit) {
    Column(Modifier.fillMaxSize().background(NookCream).statusBarsPadding().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        NookLogo(120)
        Spacer(Modifier.height(16.dp))
        Text("nook", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = NookText)
        Spacer(Modifier.height(28.dp))
        NookFilledButton("Log in", onLogin, Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))
        OrDivider()
        Spacer(Modifier.height(20.dp))
        NookOutlinedButton("Sign up", onSignUp, Modifier.fillMaxWidth())
        Spacer(Modifier.height(28.dp))
        Text("or continue with", color = NookMuted, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))
        SocialRow()
    }
}

@Composable
fun SignInScreen(onLogin: (String, String) -> Unit, onSignUp: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(NookCream)
            .statusBarsPadding()
            .padding(28.dp)
            .imePadding(),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Sign in", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = NookText)
        Spacer(Modifier.height(32.dp))
        NookField(email, { email = it }, "Enter your email or phone number")
        Spacer(Modifier.height(16.dp))
        NookField(password, { password = it }, "Password", isPassword = true)
        Spacer(Modifier.height(24.dp))
        NookFilledButton("Log in", { onLogin(email, password) }, Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        OrDivider()
        Spacer(Modifier.height(16.dp))
        NookOutlinedButton("Sign up", onSignUp, Modifier.fillMaxWidth())
        Spacer(Modifier.height(28.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("or continue with", color = NookMuted, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            SocialRow()
        }
    }
}

@Composable
fun SignUpScreen(onSignUp: (String, String) -> Unit, onLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(NookCream)
            .statusBarsPadding()
            .padding(28.dp)
            .imePadding(),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Sign up", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = NookText)
        Spacer(Modifier.height(32.dp))
        NookField(email, { email = it }, "Enter your email or phone number")
        Spacer(Modifier.height(16.dp))
        NookField(password, { password = it }, "Password", isPassword = true)
        Spacer(Modifier.height(16.dp))
        NookField(confirmPassword, { confirmPassword = it }, "Confirm Password", isPassword = true)
        Spacer(Modifier.height(24.dp))
        NookFilledButton("Sign up", { onSignUp(email, password) }, Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        OrDivider()
        Spacer(Modifier.height(16.dp))
        NookOutlinedButton("Log in", onLogin, Modifier.fillMaxWidth())
        Spacer(Modifier.height(28.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("or continue with", color = NookMuted, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            SocialRow()
        }
    }
}
